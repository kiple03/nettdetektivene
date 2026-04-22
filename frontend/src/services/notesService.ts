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
};

const EMPTY_NOTES: NotesMap = {
    nyheter: [],
    bilder: [],
    epost: [],
    passord: [],
    nett: [],
    sosiale: [],
    egne: [],
};

export async function getMyNotes(): Promise<NotesMap> {
    const res = await apiClient.get<NotesOverviewResponse>("/v1/notes/me");
    const reflections = res.data.reflections ?? {};

    return {
        ...EMPTY_NOTES,
        ...Object.fromEntries(
            Object.entries(reflections).map(([tab, notes]) => [
                tab,
                (notes ?? []).map((n) => ({ id: n.id, content: n.content })),
            ])
        ),
    };
}

export async function saveMyNote(
    category: TabId,
    content: string
): Promise<NoteEntry> {
    const res = await apiClient.post<NoteResponse>(`/v1/notes/me/${category}`, {
        content,
    });

    return { id: res.data.id, content: res.data.content };
}

export async function deleteMyNote(noteId: string): Promise<void> {
    await apiClient.delete(`/v1/notes/me/${noteId}`);
}