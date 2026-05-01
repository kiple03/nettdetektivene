import apiClient from "@/api/client.ts";

export type TabId =
    | "nyheter"
    | "bilder"
    | "epost"
    | "passord"
    | "nett"
    | "sosiale"
    | "egne";

export type NoteEntry = {
    id: string;
    content: string;
    createdAt: string;
};

export type NotesMap = Record<TabId, NoteEntry[]>;

type NoteResponse = {
    id: string;
    category: TabId;
    content: string;
    createdAt: string;
    updatedAt: string;
};

type NotesOverviewResponse = {
    reflections?: Partial<Record<TabId, NoteResponse[]>>;
    levelNotes?: Partial<Record<TabId, NoteResponse[]>>;
};

export type NotesData = {
    reflections: NotesMap;
    levelNotes: NotesMap;
};

/** Returns a NotesMap with empty arrays for all tab categories. */
export const EMPTY_NOTES_MAP = (): NotesMap => ({
    nyheter: [],
    bilder: [],
    epost: [],
    passord: [],
    nett: [],
    sosiale: [],
    egne: [],
});

/**
 * Converts a raw API response map into a fully-keyed NotesMap, filling missing tabs with empty arrays.
 * @param raw - Partial map from the API response; may be undefined.
 */
export function toNotesMap(raw: Partial<Record<TabId, NoteResponse[]>> | undefined): NotesMap {
    const base = EMPTY_NOTES_MAP();
    if (!raw) return base;

    return {
        ...base,
        ...Object.fromEntries(
            Object.entries(raw).map(([tab, notes]) => [
                tab,
                (notes ?? []).map((n) => ({ id: n.id, content: n.content, createdAt: n.createdAt })),
            ])
        ),
    };
}

/** Fetches the current user's reflections and level notes. */
export async function getMyNotes(): Promise<NotesData> {
    const res = await apiClient.get<NotesOverviewResponse>("/v1/notes/me");

    return {
        reflections: toNotesMap(res.data.reflections),
        levelNotes: toNotesMap(res.data.levelNotes),
    };
}

/**
 * Creates or updates a note for the given category.
 * @param category - The tab/category the note belongs to.
 * @param content - Note text content.
 */
export async function saveMyNote(
    category: TabId,
    content: string
): Promise<NoteEntry> {
    const res = await apiClient.post<NoteResponse>(`/v1/notes/me/${category}`, {
        content,
    });

    return { id: res.data.id, content: res.data.content, createdAt: res.data.createdAt };
}

/** Deletes a note by its ID. */
export async function deleteMyNote(noteId: string): Promise<void> {
    await apiClient.delete(`/v1/notes/me/${noteId}`);
}