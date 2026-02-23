package com.scriptuotyper.dto.ranking;

public record RankingEntryResponse(
        int rank,
        Long userId,
        String name,
        int completedChapters
) {
}
