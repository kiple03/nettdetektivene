# Database Plan

## Design principles

- **Global** (on the student account): avatar appearance, unlocked avatar items.
- **Per-enrollment** (scoped to a classroom): task progress, medals, notepad, leaderboard score.
- A student can only be in **one active classroom at a time**. `enrollment.left_at = NULL` means currently active.
- Game content (stops, tasks) is **teacher-created per classroom**. No predefined global templates.
- Task content is stored as `content_json` (JSON column) to handle all task types flexibly without separate tables per type.
- **Single user table** with a `role` column (`student`/`teacher`) instead of separate tables. Chosen to align with Spring Security's role-based auth model.

---

## Schema

```sql
-- ============================================================
-- GLOBAL: Identity
-- ============================================================

CREATE TABLE app_user (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    role          ENUM('student', 'teacher') NOT NULL,
    username      VARCHAR(100) NOT NULL,
    email         VARCHAR(255),        -- teachers only
    password_hash VARCHAR(255),        -- teachers only
    feide_id      VARCHAR(255),        -- students only
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- GLOBAL: Avatar
-- ============================================================

-- Catalog of all unlockable cosmetic items.
CREATE TABLE avatar_item (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(100) NOT NULL,
    category         VARCHAR(50)  NOT NULL,  -- e.g. 'hat', 'clothing', 'accessory'
    unlock_condition VARCHAR(255)             -- e.g. 'earn 3 medals', 'complete final boss'
);

-- Which items a student has unlocked, and which one is currently equipped per category.
-- is_equipped = TRUE means this item is the one the student is currently wearing.
-- Current appearance is derived by querying WHERE is_equipped = TRUE for a given student.
CREATE TABLE user_avatar_unlock (
    user_id        BIGINT    NOT NULL,
    avatar_item_id BIGINT    NOT NULL,
    unlocked_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_equipped    BOOLEAN   NOT NULL DEFAULT FALSE,
    PRIMARY KEY (user_id, avatar_item_id),
    FOREIGN KEY (user_id)        REFERENCES app_user(id)    ON DELETE CASCADE,
    FOREIGN KEY (avatar_item_id) REFERENCES avatar_item(id) ON DELETE CASCADE
);

-- ============================================================
-- CLASSROOM
-- ============================================================

CREATE TABLE classroom (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    description TEXT,
    join_code   VARCHAR(20)  NOT NULL UNIQUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- A classroom can have multiple teachers; one is the owner.
CREATE TABLE classroom_teacher (
    classroom_id BIGINT  NOT NULL,
    user_id      BIGINT  NOT NULL,
    is_owner     BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (classroom_id, user_id),
    FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id)      REFERENCES app_user(id)  ON DELETE CASCADE
);

-- Student enrollment. left_at NULL = currently active. One active enrollment at a time.
CREATE TABLE enrollment (
    id           BIGINT    AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT    NOT NULL,
    classroom_id BIGINT    NOT NULL,
    enrolled_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    left_at      TIMESTAMP NULL,
    FOREIGN KEY (user_id)      REFERENCES app_user(id)  ON DELETE CASCADE,
    FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE CASCADE
);

-- ============================================================
-- GAME CONTENT (teacher-created, per classroom)
-- ============================================================

CREATE TABLE stop (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id  BIGINT       NOT NULL,
    title         VARCHAR(200) NOT NULL,
    description   TEXT,
    display_order INT          NOT NULL,
    FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE CASCADE
);

-- content_json stores all task-type-specific data.
-- Example: { "type": "news", "articles": [{ "text": "...", "is_real": true }, ...] }
-- Example: { "type": "image", "images": [{ "url": "...", "label": "ai_generated" }, ...] }
CREATE TABLE task (
    id            BIGINT    AUTO_INCREMENT PRIMARY KEY,
    stop_id       BIGINT    NOT NULL,
    display_order INT       NOT NULL,
    difficulty    TINYINT   NOT NULL DEFAULT 1,
    content_json  JSON      NOT NULL,
    FOREIGN KEY (stop_id) REFERENCES stop(id) ON DELETE CASCADE
);

-- ============================================================
-- PROGRESS (per enrollment)
-- ============================================================

CREATE TABLE task_progress (
    id            BIGINT    AUTO_INCREMENT PRIMARY KEY,
    enrollment_id BIGINT    NOT NULL,
    task_id       BIGINT    NOT NULL,
    completed_at  TIMESTAMP NULL,
    attempts      INT       NOT NULL DEFAULT 0,
    UNIQUE (enrollment_id, task_id),
    FOREIGN KEY (enrollment_id) REFERENCES enrollment(id) ON DELETE CASCADE,
    FOREIGN KEY (task_id)       REFERENCES task(id)       ON DELETE CASCADE
);

-- One medal per stop per enrollment, earned when all tasks in that stop are completed.
CREATE TABLE medal (
    id            BIGINT    AUTO_INCREMENT PRIMARY KEY,
    enrollment_id BIGINT    NOT NULL,
    stop_id       BIGINT    NOT NULL,
    earned_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (enrollment_id, stop_id),
    FOREIGN KEY (enrollment_id) REFERENCES enrollment(id) ON DELETE CASCADE,
    FOREIGN KEY (stop_id)       REFERENCES stop(id)       ON DELETE CASCADE
);

-- ============================================================
-- NOTEPAD (per enrollment)
-- ============================================================

-- type 'auto'    = theory/tips added automatically by the system when a task is completed.
-- type 'student' = free-text reflection written by the student.
CREATE TABLE notepad_entry (
    id            BIGINT                       AUTO_INCREMENT PRIMARY KEY,
    enrollment_id BIGINT                       NOT NULL,
    stop_id       BIGINT                       NULL,  -- NULL = general / not tied to a stop
    type          ENUM('auto', 'student')      NOT NULL,
    content       TEXT                         NOT NULL,
    created_at    TIMESTAMP                    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP                    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (enrollment_id) REFERENCES enrollment(id) ON DELETE CASCADE,
    FOREIGN KEY (stop_id)       REFERENCES stop(id)       ON DELETE SET NULL
);

-- ============================================================
-- WEEKLY MYSTERY (per classroom, submitted via enrollment)
-- ============================================================

CREATE TABLE mystery_submission (
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id          BIGINT       NOT NULL,
    content                TEXT,
    image_url              VARCHAR(500),
    submitted_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status                 ENUM('pending', 'approved', 'rejected') NOT NULL DEFAULT 'pending',
    reviewed_by_teacher_id BIGINT       NULL,
    reviewed_at            TIMESTAMP    NULL,
    is_featured            BOOLEAN      NOT NULL DEFAULT FALSE,  -- shown as "ukens mysterium"
    FOREIGN KEY (enrollment_id)          REFERENCES enrollment(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_by_teacher_id) REFERENCES app_user(id) ON DELETE SET NULL
);
```

---

## Notes

- **Leaderboard scores** are derived (not stored): count completed stops/medals per enrollment for individual ranking; sum across all enrollments in a classroom for classroom-vs-classroom ranking.
- **Notifications** to teachers (student stuck, completed stop, inappropriate content, mystery submission) are application-level events triggered by changes to `task_progress`, `mystery_submission`, and `notepad_entry` — no separate notification table needed for MVP.
- **Feide login for students** is simulated per the vision document requirements.
