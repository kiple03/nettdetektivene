import { useEffect, useMemo, useState } from "react";
import overlayStyles from "./NotebookOverlay.module.css";
import styles from "@/pages/student/NotesPage.module.css";
import { Button } from "@/components/ui/buttons/Button.tsx";
import TextArea from "@/components/ui/textArea/TextArea.tsx";
import {
    getMyNotes,
    saveMyNote,
    deleteMyNote,
    type TabId,
    type NotesMap,
    EMPTY_NOTES_MAP,
} from "@/api/notesService";
import { LayeredAvatar } from "@/components/miscellaneous/LayeredAvatar";
import { useUserProfile } from "@/hooks/useUserProfile.ts";

interface TabConfig {
    id: TabId;
    label: string;
    icon: string;
    color: string;
    placeholder: string;
}

type SaveState = "idle" | "saving" | "saved" | "error";

const TAB_ORDER: TabId[] = [
    "nyheter",
    "bilder",
    "epost",
    "passord",
    "nett",
    "sosiale",
    "egne",
];

const TABS: TabConfig[] = [
    { id: "nyheter", label: "Nyheter", icon: "📰", color: "#E57373", placeholder: "Hva har du lært om falske nyheter?" },
    { id: "bilder", label: "Bilder", icon: "📸", color: "#9C27B0", placeholder: "Hva hjelper deg å se om et bilde er ekte?" },
    { id: "epost", label: "E-post", icon: "✉️", color: "#2196F3", placeholder: "Hva gjør du om du får en mistenkelig e-post?" },
    { id: "passord", label: "Passord", icon: "🔐", color: "#4CAF50", placeholder: "Hva kjennetegner et godt passord?" },
    { id: "nett", label: "Nettbutikk", icon: "🛒", color: "#FF9800", placeholder: "Hva ser du etter på en nettbutikk?" },
    { id: "sosiale", label: "Sosiale medier", icon: "💬", color: "#F44336", placeholder: "Hva gjør du om venner deler noe mistenkelig?" },
    { id: "egne", label: "Mine tanker", icon: "✏️", color: "#607D8B", placeholder: "Skriv hva du har oppdaget eller lurt på..." },
];

function getTabConfig(tabId: TabId): TabConfig {
    return TABS.find((tab) => tab.id === tabId)!;
}

function formatDate(iso: string | null | undefined): string {
    if (!iso) return "";
    const d = new Date(iso);
    if (isNaN(d.getTime()) || d.getFullYear() < 2000) return "";
    return new Intl.DateTimeFormat("nb-NO", {
        day: "numeric", month: "short", year: "numeric",
        hour: "2-digit", minute: "2-digit",
    }).format(d);
}

function getPossessive(name: string): string {
    return /[sxzSXZ]$/.test(name) ? `${name}'` : `${name}`;
}

const Spirals = ({ count = 16 }: { count?: number }) => (
    <div className={styles.spiralStrip}>
        {Array.from({ length: count }).map((_, i) => (
            <div key={i} className={styles.spiralCoil} />
        ))}
    </div>
);

type Props = {
    onClose: () => void;
    initialTab?: TabId;
};

export default function NotebookOverlay({ onClose, initialTab = "nyheter" }: Props) {
    const { data: userProfile } = useUserProfile();
    const username = userProfile?.username;
    const notebookTitle = username
        ? `${getPossessive(username)} sin notatblokk`
        : "Mi notatblokk";

    const [activeTab, setActiveTab] = useState<TabId>(initialTab);
    const [draft, setDraft] = useState("");
    const [savedNotes, setSavedNotes] = useState<NotesMap>(EMPTY_NOTES_MAP());
    const [levelNotes, setLevelNotes] = useState<NotesMap>(EMPTY_NOTES_MAP());
    const [isLoading, setIsLoading] = useState(true);
    const [loadError, setLoadError] = useState<string | null>(null);
    const [saveState, setSaveState] = useState<SaveState>("idle");

    const activeConfig = useMemo(
        () => TABS.find((tab) => tab.id === activeTab)!,
        [activeTab]
    );

    useEffect(() => {
        setDraft("");
        setSaveState("idle");
    }, [activeTab]);

    useEffect(() => {
        let isMounted = true;

        async function loadNotes() {
            try {
                setIsLoading(true);
                setLoadError(null);
                const data = await getMyNotes();
                if (!isMounted) return;
                setSavedNotes(data.reflections);
                setLevelNotes(data.levelNotes);
            } catch (error) {
                console.error("Failed to load notes", error);
                if (!isMounted) return;
                setLoadError("Kunne ikke laste notatene dine.");
            } finally {
                if (isMounted) setIsLoading(false);
            }
        }

        loadNotes();
        return () => { isMounted = false; };
    }, []);

    const byNewest = (a: { createdAt: string }, b: { createdAt: string }) =>
        new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();

    const visibleNotes = useMemo(() => {
        if (activeTab === "egne") {
            const relevantTabs = TAB_ORDER.filter((t) => t !== "egne");
            const own = relevantTabs
                .flatMap((tabId) => savedNotes[tabId].map((note) => ({ ...note, category: tabId, isLevel: false })))
                .sort(byNewest);
            const level = relevantTabs
                .flatMap((tabId) => levelNotes[tabId].map((note) => ({ ...note, category: tabId, isLevel: true })))
                .sort(byNewest);
            return { own, level };
        }
        return {
            own: savedNotes[activeTab]
                .map((note) => ({ ...note, category: activeTab, isLevel: false }))
                .sort(byNewest),
            level: levelNotes[activeTab]
                .map((note) => ({ ...note, category: activeTab, isLevel: true }))
                .sort(byNewest),
        };
    }, [activeTab, savedNotes, levelNotes]);

    const handleDraftChange = (value: string) => {
        if (saveState === "saved" || saveState === "error") setSaveState("idle");
        setDraft(value);
    };

    const handleSave = async () => {
        const trimmed = draft.trim();
        if (!trimmed) return;
        try {
            setSaveState("saving");
            const newNote = await saveMyNote(activeTab, trimmed);
            setSavedNotes((prev) => ({
                ...prev,
                [activeTab]: [...prev[activeTab], newNote],
            }));
            setDraft("");
            setSaveState("saved");
            window.setTimeout(() => setSaveState("idle"), 1400);
        } catch (error) {
            console.error("Failed to save note", error);
            setSaveState("error");
        }
    };

    const handleDelete = async (noteId: string) => {
        try {
            await deleteMyNote(noteId);
            setSavedNotes((prev) => ({
                ...prev,
                [activeTab]: prev[activeTab].filter((n) => n.id !== noteId),
            }));
        } catch (error) {
            console.error("Failed to delete note", error);
        }
    };

    const topStatusText = (() => {
        if (isLoading) return "Laster notater...";
        if (loadError) return loadError;
        return null;
    })();

    const hasAnyNotes = visibleNotes.own.length > 0 || visibleNotes.level.length > 0;
    const isEgneTab = activeTab === "egne";

    return (
        <div className={overlayStyles.backdrop} onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}>
            <div className={overlayStyles.container}>
                <div className={styles.notebookOpen}>
                    <div className={styles.openBook}>
                        <div className={styles.bookLeft}>
                            <Spirals count={16} />
                            <span className={styles.bookLeftTitle}>Notatblokk</span>
                        </div>

                        <div className={styles.bookPages}>
                            <Button
                                variant="destructive"
                                className={styles.closeBtn}
                                onClick={onClose}
                                aria-label="Lukk notatblokken"
                            >
                                ✕
                            </Button>

                            <div className={styles.pageContent}>
                                <div className={styles.pageHeader}>
                                    <LayeredAvatar
                                        items={userProfile?.equippedItems ?? []}
                                        size={55}
                                        hairColor={userProfile?.hairColor}
                                    />
                                    <div>
                                        <h2 className={styles.pageHeaderTitle}>{notebookTitle}</h2>
                                        <p className={styles.pageHeaderSub}>Her lagres alt du lærer!</p>
                                        {topStatusText && (
                                            <p
                                                className={styles.pageHeaderSub}
                                                style={{ color: loadError ? "#c62828" : undefined }}
                                            >
                                                {topStatusText}
                                            </p>
                                        )}
                                    </div>
                                </div>

                                <div className={styles.pageSpread}>
                                    {!isEgneTab && (
                                        <div className={styles.leftNotebookPage}>
                                            <div className={styles.reflectionBox}>
                                                <TextArea
                                                    id={`reflection-${activeTab}`}
                                                    label="✏️ Skriv din egen refleksjon:"
                                                    placeholder={activeConfig.placeholder}
                                                    value={draft}
                                                    onChange={(e) => handleDraftChange(e.target.value)}
                                                    labelClassName={styles.reflectionLabel}
                                                    className={styles.reflectionTextarea}
                                                />
                                                <div className={styles.saveRow}>
                                                    <Button
                                                        variant="unstyled"
                                                        onClick={handleSave}
                                                        disabled={
                                                            isLoading ||
                                                            !!loadError ||
                                                            saveState === "saving" ||
                                                            !draft.trim()
                                                        }
                                                        className={styles.saveButton}
                                                    >
                                                        {saveState === "saving" ? "Lagrer..." : "Lagre"}
                                                    </Button>
                                                    {saveState === "saved" && (
                                                        <span className={styles.savedText}>Lagret!</span>
                                                    )}
                                                    {saveState === "error" && (
                                                        <span className={styles.errorText}>Kunne ikke lagre</span>
                                                    )}
                                                </div>
                                            </div>
                                        </div>
                                    )}

                                    <div className={isEgneTab ? styles.fullNotebookPage : styles.rightNotebookPage}>
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

                                        <div className={styles.savedNotesPanel}>
                                            {!hasAnyNotes ? (
                                                <div className={styles.emptyState}>
                                                    Ingen lagrede notater her ennå.
                                                </div>
                                            ) : (
                                                <>
                                                    {visibleNotes.level.length > 0 && (
                                                        <>
                                                            <p className={styles.notesSectionHeader}>📌 Fra nivået</p>
                                                            {visibleNotes.level.map((note) => {
                                                                const tab = getTabConfig(note.category);
                                                                return (
                                                                    <div key={note.id} className={styles.savedNoteCard}>
                                                                        {isEgneTab && (
                                                                            <div
                                                                                className={styles.savedNoteHeader}
                                                                                style={{ borderLeft: `10px solid ${tab.color}`, paddingLeft: "10px" }}
                                                                            >
                                                                                <span>{tab.icon}</span>
                                                                                <span>{tab.label}</span>
                                                                            </div>
                                                                        )}
                                                                        <p className={styles.savedNoteText}>{note.content}</p>
                                                                        {formatDate(note.createdAt) && <p className={styles.noteTimestamp}>{formatDate(note.createdAt)}</p>}
                                                                    </div>
                                                                );
                                                            })}
                                                        </>
                                                    )}

                                                    {visibleNotes.own.length > 0 && (
                                                        <>
                                                            <p className={styles.notesSectionHeader}>✏️ Mine notater</p>
                                                            {visibleNotes.own.map((note) => {
                                                                const tab = getTabConfig(note.category);
                                                                return (
                                                                    <div key={note.id} className={styles.savedNoteCard}>
                                                                        {isEgneTab && (
                                                                            <div
                                                                                className={styles.savedNoteHeader}
                                                                                style={{ borderLeft: `10px solid ${tab.color}`, paddingLeft: "10px" }}
                                                                            >
                                                                                <span>{tab.icon}</span>
                                                                                <span>{tab.label}</span>
                                                                            </div>
                                                                        )}
                                                                        <p className={styles.savedNoteText}>{note.content}</p>
                                                                        {formatDate(note.createdAt) && <p className={styles.noteTimestamp}>{formatDate(note.createdAt)}</p>}
                                                                        <Button
                                                                            variant="destructive"
                                                                            className={styles.deleteButton}
                                                                            onClick={() => handleDelete(note.id)}
                                                                            aria-label="Slett notat"
                                                                            style={{ marginTop: "8px", padding: "2px 10px", fontSize: "0.8rem" }}
                                                                        >
                                                                            ✕ Slett
                                                                        </Button>
                                                                    </div>
                                                                );
                                                            })}
                                                        </>
                                                    )}
                                                </>
                                            )}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
