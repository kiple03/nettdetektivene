import { useState } from "react";
import styles from "./NotesPage.module.css";
import { Button } from "@/components/ui/buttons/Button.tsx";
import BackButton from "@/components/ui/buttons/backButton/BackButton.tsx";

// ── Images ─────────────────────────────────────────────────────────────────
import detectiveImg from "@/assets/images/joinWithCode/detective.png";
import backgroundImg from "@/assets/detective_room_interior.png";

// ── Types ───────────────────────────────────────────────────────────────────
type TabId =
    | "nyheter"
    | "bilder"
    | "epost"
    | "passord"
    | "nett"
    | "sosiale"
    | "egne";

interface TabConfig {
    id: TabId;
    label: string;
    icon: string;
    color: string;
    tips: { icon: string; title: string; body: string }[];
    placeholder: string;
}

// ── Tab configuration ────────────────────────────────────────────────────────
const TABS: TabConfig[] = [
    {
        id: "nyheter",
        label: "Nyheter",
        icon: "📰",
        color: "#E57373",
        tips: [
            {
                icon: "🔎",
                title: "Se på overskriften!",
                body: "Er den veldig skremmende eller overraskende? Det kan være falsk nyhet!",
            },
            {
                icon: "🤔",
                title: "Hvem har skrevet dette?",
                body: "Finn ut om avisen er kjent og pålitelig.",
            },
            {
                icon: "❤️‍🔥",
                title: "Føler du deg sint eller redd?",
                body: "Falske nyheter bruker sterke følelser for å lure deg!",
            },
        ],
        placeholder: "Hva har du lært om falske nyheter?",
    },
    {
        id: "bilder",
        label: "Bilder",
        icon: "📸",
        color: "#9C27B0",
        tips: [
            {
                icon: "🖼️",
                title: "Ikke alle bilder er ekte!",
                body: "Kunstig intelligens kan lage bilder av ting som aldri har skjedd.",
            },
            {
                icon: "👁️",
                title: "Se på hendene og ørene!",
                body: "KI-bilder har ofte rare fingre eller uskarp bakgrunn.",
            },
        ],
        placeholder: "Hva hjelper deg å se om et bilde er ekte?",
    },
    {
        id: "epost",
        label: "E-post",
        icon: "✉️",
        color: "#2196F3",
        tips: [
            {
                icon: "📧",
                title: "Sjekk e-postadressen!",
                body: "Er det rare bokstaver i adressen? Da er det mistenkelig!",
            },
            {
                icon: "🔗",
                title: "Ikke klikk på lenker!",
                body: "Spør alltid en voksen før du klikker på en ukjent lenke.",
            },
        ],
        placeholder: "Hva gjør du om du får en mistenkelig e-post?",
    },
    {
        id: "passord",
        label: "Passord",
        icon: "🔐",
        color: "#4CAF50",
        tips: [
            {
                icon: "💪",
                title: "Et sterkt passord er langt!",
                body: "Bruk store og små bokstaver + tall + tegn. F.eks: Hund!Løper7",
            },
            {
                icon: "🚫",
                title: "Ikke bruk samme passord overalt!",
                body: "Ikke bruk navn, bursdag eller «123456».",
            },
        ],
        placeholder: "Hva kjennetegner et godt passord?",
    },
    {
        id: "nett",
        label: "Nettbutikk",
        icon: "🛒",
        color: "#FF9800",
        tips: [
            {
                icon: "🔒",
                title: "Sjekk adressen i nettleseren!",
                body: 'En trygg nettside starter med "https://" og har et hengelås-symbol.',
            },
            {
                icon: "💸",
                title: "For godt til å være sant?",
                body: "Kjempestore rabatter kan være lureri!",
            },
        ],
        placeholder: "Hva ser du etter på en nettbutikk?",
    },
    {
        id: "sosiale",
        label: "Sosiale medier",
        icon: "💬",
        color: "#F44336",
        tips: [
            {
                icon: "🛑",
                title: "Tenk før du deler!",
                body: "Er du sikker på at det er sant? Sjekk kilden først.",
            },
            {
                icon: "👥",
                title: "Gruppepress betyr ikke at noe er sant!",
                body: "Mange deler noe → det betyr ikke at det er ekte.",
            },
        ],
        placeholder: "Hva gjør du om venner deler noe mistenkelig?",
    },
    {
        id: "egne",
        label: "Mine tanker",
        icon: "✏️",
        color: "#607D8B",
        tips: [
            {
                icon: "🕵️",
                title: "Skriv det du tenker på!",
                body: "Har du sett noe rart på nett? Skriv det her.",
            },
        ],
        placeholder: "Skriv hva du har oppdaget eller lurt på...",
    },
];

// ── Spiral component ─────────────────────────────────────────────────────────
const Spirals = ({ count = 16 }: { count?: number }) => (
    <div className={styles.spiralStrip}>
        {Array.from({ length: count }).map((_, i) => (
            <div key={i} className={styles.spiralCoil} />
        ))}
    </div>
);

// ── Main component ───────────────────────────────────────────────────────────
export default function NotesPage() {
    const [isOpen, setIsOpen] = useState(false);
    const [activeTab, setActiveTab] = useState<TabId>("nyheter");
    const [reflections, setReflections] = useState<Record<TabId, string>>(
        {} as Record<TabId, string>
    );

    const activeConfig = TABS.find((t) => t.id === activeTab)!;

    return (
        <div
            className={styles.pageWrapper}
            style={{
                backgroundImage: `url(${backgroundImg})`,
                backgroundSize: "cover",
                backgroundPosition: "center",
            }}
        >
            <h1 className={styles.sceneTitle}>🔍 Nettdetektivene</h1>

            {/* ── CLOSED NOTEBOOK ── */}
            {!isOpen && (
                <div className={styles.notebookScene}>
                    <div
                        className={styles.notebookClosed}
                        onClick={() => setIsOpen(true)}
                        role="button"
                        tabIndex={0}
                        onKeyDown={(e) => e.key === "Enter" && setIsOpen(true)}
                        aria-label="Åpne notatblokken"
                    >
                        <Spirals />

                        <div className={styles.cover}>
                            <span className={styles.coverTitle}>Notatblokk</span>

                            <img
                                src={detectiveImg}
                                alt="Nettdetektiv"
                                className={styles.coverMascot}
                            />

                            <span className={styles.coverCopyright}>
                                © 2026 Nettdetektivene
                            </span>
                        </div>

                        <div className={styles.clickHint}>👆 Trykk for å åpne!</div>
                    </div>
                </div>
            )}

            {/* ── OPEN NOTEBOOK ── */}
            {isOpen && (
                <div className={styles.notebookOpen}>
                    <div className={styles.openBook}>

                        {/* Left spine */}
                        <div className={styles.bookLeft}>
                            <Spirals count={16} />
                            <span className={styles.bookLeftTitle}>Notatblokk</span>
                        </div>

                        {/* Paper area */}
                        <div className={styles.bookPages}>

                            {/* Close button — reuses Button with destructive variant */}
                            <Button
                                variant="destructive"
                                className={styles.closeBtn}
                                onClick={() => setIsOpen(false)}
                                aria-label="Lukk notatblokken"
                            >
                                ✕
                            </Button>

                            <div className={styles.pageContent}>

                                {/* Header */}
                                <div className={styles.pageHeader}>
                                    <span className={styles.pageHeaderIcon}>📓</span>
                                    <div>
                                        <h2 className={styles.pageHeaderTitle}>Mi notatblokk</h2>
                                        <p className={styles.pageHeaderSub}>
                                            Her lagres alt du lærer!
                                        </p>
                                    </div>
                                </div>

                                {/* Tab strip — reuses Button with unstyled variant + inline color */}
                                <div className={styles.tabStrip}>
                                    {TABS.map((tab) => (
                                        <Button
                                            key={tab.id}
                                            variant="unstyled"
                                            className={styles.tabBtn}
                                            style={
                                                activeTab === tab.id
                                                    ? { background: tab.color, borderColor: tab.color, color: "#fff" }
                                                    : { color: tab.color, borderColor: tab.color }
                                            }
                                            onClick={() => setActiveTab(tab.id)}
                                        >
                                            {tab.icon} {tab.label}
                                        </Button>
                                    ))}
                                </div>

                                {/* Tips */}
                                <div className={styles.tipsArea}>
                                    {activeConfig.tips.map((tip, i) => (
                                        <div
                                            key={i}
                                            className={styles.tipCard}
                                            style={{ borderLeftColor: activeConfig.color }}
                                        >
                                            <span className={styles.tipIcon}>{tip.icon}</span>
                                            <div className={styles.tipText}>
                                                <strong className={styles.tipTitle}>{tip.title}</strong>
                                                {tip.body}
                                            </div>
                                        </div>
                                    ))}
                                </div>

                                {/* Reflection box */}
                                <div className={styles.reflectionBox}>
                                    <label className={styles.reflectionLabel}>
                                        ✏️ Skriv din egen refleksjon:
                                    </label>
                                    <textarea
                                        className={styles.reflectionTextarea}
                                        placeholder={activeConfig.placeholder}
                                        value={reflections[activeTab] ?? ""}
                                        onChange={(e) =>
                                            setReflections((prev) => ({
                                                ...prev,
                                                [activeTab]: e.target.value,
                                            }))
                                        }
                                    />
                                </div>

                            </div>
                        </div>
                    </div>
                </div>
            )}

            {/* ── BACK BUTTON — fixed bottom-left corner ── */}
            <div className={styles.backButtonWrapper}>
                <BackButton ariaLabel="Gå tilbake til forrige side" />
            </div>
        </div>
    );
}