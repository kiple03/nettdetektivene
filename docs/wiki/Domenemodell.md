# Domenemodell

## Oversikt

Domenemodellen viser de sentrale entitetene i Nettdetektivene og relasjonene mellom dem. Systemet er bygget rundt tre hovedomraader: **brukere og klasserom**, **spillogikk** og **avatar/gamification**.

## Domenediagram (Mermaid)

```mermaid
classDiagram
    direction TB

    class User {
        UUID id
        RoleEnum role
        String username
        String email
        String passwordHash
        Instant createdAt
    }

    class RoleEnum {
        <<enumeration>>
        TEACHER
        STUDENT
    }

    class Classroom {
        UUID id
        String title
        String description
        String joinCode
        LocalDateTime createdAt
    }

    class ClassroomTeacher {
        UUID classroomId
        UUID userId
        boolean isOwner
    }

    class Enrollment {
        UUID id
        Instant enrolledAt
        Instant leftAt
    }

    class Stop {
        int id
        String name
        int displayOrder
    }

    class Level {
        int id
        String title
        String description
        LevelType levelType
    }

    class LevelType {
        <<enumeration>>
        MULTIPLE_CHOICE
        TEXT_QUIZ
        PICTURE_QUIZ
        MINIGAME
        BOSS_BATTLE
        TUTORIAL
    }

    class Question {
        int id
        String questionText
        JSON optionContent
    }

    class LevelCompletion {
        UUID id
        Instant completedAt
    }

    class MysterySubmission {
        UUID id
        String content
        String imageUrl
        Instant submittedAt
        MysteryStatusEnum status
        Instant reviewedAt
        boolean featured
    }

    class MysteryStatusEnum {
        <<enumeration>>
        PENDING
        APPROVED
        REJECTED
    }

    class AvatarItem {
        UUID id
        String name
        String category
        String unlockCondition
    }

    class UserInventoryItem {
        UUID id
        LocalDateTime unlockedAt
        boolean isEquipped
    }

    class ImageEntity {
        Long id
        String objectKey
        String originalFilename
        String contentType
        Long size
        LocalDateTime uploadedAt
    }

    User --> RoleEnum
    User "1" -- "*" ClassroomTeacher : administrerer
    Classroom "1" -- "*" ClassroomTeacher
    User "1" -- "*" Enrollment : er innmeldt i
    Classroom "1" -- "*" Enrollment
    Stop "1" -- "*" Level : inneholder
    Level --> LevelType
    Level "1" -- "*" Question : har
    Enrollment "1" -- "*" LevelCompletion : fullforer
    Level "1" -- "*" LevelCompletion
    Enrollment "1" -- "*" MysterySubmission : sender inn
    MysterySubmission --> MysteryStatusEnum
    User "1" -- "*" MysterySubmission : vurderer (laerer)
    User "1" -- "*" UserInventoryItem : eier
    AvatarItem "1" -- "*" UserInventoryItem
```

## Beskrivelse av entiteter

### Brukere og klasserom

| Entitet | Beskrivelse |
|---------|-------------|
| **User** | Representerer baade elever og laerere. Rollen bestemmes av `RoleEnum`. |
| **Classroom** | Et klasserom opprettet av en laerer, med unik `joinCode` som elever bruker for aa bli med. |
| **ClassroomTeacher** | Kobling mellom laerer og klasserom. `isOwner` markerer hvem som opprettet rommet. Flere laerere kan administrere samme rom. |
| **Enrollment** | Kobling mellom elev og klasserom. `leftAt` settes naar en elev forlater rommet. |

### Spillogikk

| Entitet | Beskrivelse |
|---------|-------------|
| **Stop** | Et stoppested paa kartet (f.eks. Nyhetskvartalet, Fotografen, Postkontoret). `displayOrder` styrer rekkefølgen. |
| **Level** | En oppgave innenfor et stoppested. Hvert stoppested har 3 nivåer med oekende vanskelighetsgrad. `LevelType` bestemmer oppgavetypen. |
| **Question** | Et spoersmaal innenfor et nivaa. `optionContent` lagres som JSON med svaralternativer (MultipleChoiceOption). |
| **LevelCompletion** | Registrerer naar en elev (via enrollment) fullforer et nivaa. Unik kombinasjon av enrollment + level. |

### Ukens mysterium

| Entitet | Beskrivelse |
|---------|-------------|
| **MysterySubmission** | En elevs innsending til ukens mysterium. Kan inneholde tekst og bilde. Status gaar fra PENDING til APPROVED/REJECTED etter laerevurdering. `featured` markerer om den vises som ukens mysterium. |

### Avatar og gamification

| Entitet | Beskrivelse |
|---------|-------------|
| **AvatarItem** | Et tilgjengelig avatar-element (haarsil, klær, tilbehoer osv.) med kategori og opplaasningsbetingelse. |
| **UserInventoryItem** | Kobling mellom bruker og opplaaste avatar-elementer. `isEquipped` viser om elementet er paa. |

### Media

| Entitet | Beskrivelse |
|---------|-------------|
| **ImageEntity** | Metadata for bilder lagret i S3. Brukes av oppgaver og mystery submissions. |
