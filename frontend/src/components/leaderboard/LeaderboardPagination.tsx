import styles from './LeaderboardPagination.module.css';

interface Props {
    currentPage: number;      // 0-indexed (Spring Boot default)
    totalPages: number;
    totalElements: number;
    pageSize: number;
    onPageChange: (page: number) => void;
}

function getPageNumbers(current: number, total: number): (number | '...')[] {
    if (total <= 5) {
        return Array.from({ length: total }, (_, i) => i);
    }

    const pages: (number | '...')[] = [0]; // always show first

    if (current > 2) pages.push('...');

    for (let i = Math.max(1, current - 1); i <= Math.min(total - 2, current + 1); i++) {
        pages.push(i);
    }

    if (current < total - 3) pages.push('...');

    pages.push(total - 1); // always show last

    return pages;
}

export default function LeaderboardPagination({
        currentPage,
        totalPages,
        totalElements,
        pageSize,
        onPageChange,
    }: Props) {

    const start = Math.min(currentPage * pageSize + 1, totalElements);
    const end = Math.min((currentPage + 1) * pageSize, totalElements);
    const pageNumbers = getPageNumbers(currentPage, totalPages);

    return (
        <div className={styles.wrapper}>
      <span className={styles.info}>
        Viser {start}–{end} av {totalElements} klasserom
      </span>

            <div className={styles.controls}>
                <button
                    className={styles.btn}
                    onClick={() => onPageChange(currentPage - 1)}
                    disabled={currentPage === 0}
                >
                    ‹ Tilbake
                </button>

                {pageNumbers.map((p, i) =>
                        p === '...' ? (
                            <span key={`ellipsis-${i}`} className={styles.ellipsis}>
              ...
            </span>
                        ) : (
                            <button
                                key={p}
                                className={`${styles.btn} ${p === currentPage ? styles.active : ''}`}
                                onClick={() => onPageChange(p)}
                            >
                                {p + 1}  {/* display 1-indexed to user */}
                            </button>
                        )
                )}

                <button
                    className={styles.btn}
                    onClick={() => onPageChange(currentPage + 1)}
                    disabled={currentPage === totalPages - 1}
                >
                    Neste ›
                </button>
            </div>
        </div>
    );
}