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

export const EMPTY_NOTES_MAP = (): NotesMap => ({
    nyheter: [],
    bilder: [],
    epost: [],
    passord: [],
    nett: [],
    sosiale: [],
    egne: [],
});

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

export async function getMyNotes(): Promise<NotesData> {
    const res = await apiClient.get<NotesOverviewResponse>("/v1/notes/me");

    return {
        reflections: toNotesMap(res.data.reflections),
        levelNotes: toNotesMap(res.data.levelNotes),
    };
}

export async function saveMyNote(
    category: TabId,
    content: string
): Promise<NoteEntry> {
    const res = await apiClient.post<NoteResponse>(`/v1/notes/me/${category}`, {
        content,
    });

    return { id: res.data.id, content: res.data.content, createdAt: res.data.createdAt };
}

export async function deleteMyNote(noteId: string): Promise<void> {
    await apiClient.delete(`/v1/notes/me/${noteId}`);
}