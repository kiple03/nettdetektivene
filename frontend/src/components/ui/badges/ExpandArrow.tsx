type ExpandNotesButtonProps = {
    expanded: boolean;
    onClick: () => void;
    hidden?: boolean;
};

/** Circular arrow button that toggles between expanded/collapsed state. */
export default function ExpandArrow({
                                              expanded,
                                              onClick,
                                              hidden = false,
                                          }: ExpandNotesButtonProps) {
    if (hidden) return null;

    return (
        <button
            type="button"
            onClick={onClick}
            aria-label={expanded ? "Skjul lagrede notater" : "Vis lagrede notater"}
            style={{
                width: "56px",
                height: "56px",
                borderRadius: "50%",
                border: "1px solid rgba(255,255,255,0.08)",
                background:
                    "linear-gradient(180deg, rgba(45,45,45,0.95), rgba(28,28,28,0.98))",
                display: "inline-flex",
                alignItems: "center",
                justifyContent: "center",
                cursor: "pointer",
                boxShadow: "0 8px 20px rgba(0,0,0,0.22)",
                transition: "transform 0.18s ease, box-shadow 0.18s ease",
            }}
            onMouseEnter={(e) => {
                e.currentTarget.style.transform = "translateY(-1px)";
                e.currentTarget.style.boxShadow = "0 12px 24px rgba(0,0,0,0.28)";
            }}
            onMouseLeave={(e) => {
                e.currentTarget.style.transform = "translateY(0)";
                e.currentTarget.style.boxShadow = "0 8px 20px rgba(0,0,0,0.22)";
            }}
        >
      <span
          style={{
              color: "#f3f4f6",
              fontSize: "2rem",
              lineHeight: 1,
              transform: expanded ? "rotate(180deg)" : "rotate(0deg)",
              transition: "transform 0.2s ease",
              marginTop: expanded ? "-2px" : "2px",
              userSelect: "none",
          }}
      >
        ↓
      </span>
        </button>
    );
}