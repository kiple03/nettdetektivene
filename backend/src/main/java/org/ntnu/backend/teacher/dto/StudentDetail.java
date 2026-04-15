package org.ntnu.backend.teacher.dto;

import java.util.List;

public record StudentDetail(
        String userId,
        String username,
        List<MedalEntry> medals,
        List<LevelEntry> levelCompletions,
        List<NotepadEntryItem> notepadEntries
) {
    public record MedalEntry(String stopId, String stopTitle, String earnedAt) {}
    public record LevelEntry(String levelId, String completedAt) {}
    public record NotepadEntryItem(String id, String stopId, String type, String content, String createdAt) {}
}
