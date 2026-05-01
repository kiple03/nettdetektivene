import styles from "./lock-badge.module.css";

/** Displays a lock icon overlay to indicate a locked item. */
function LockBadge() {
    return (
        <div className={styles.lockBadge} aria-hidden="true">
            <svg
                viewBox="0 0 24 24"
                className={styles.lockSvg}
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
            >
                <path
                    d="M7 10V7a5 5 0 0 1 10 0v3"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                />
                <rect
                    x="5"
                    y="10"
                    width="14"
                    height="10"
                    rx="2"
                    stroke="currentColor"
                    strokeWidth="2"
                />
                <circle cx="12" cy="15" r="1.5" fill="currentColor" />
            </svg>
        </div>
    );
}

export default LockBadge;