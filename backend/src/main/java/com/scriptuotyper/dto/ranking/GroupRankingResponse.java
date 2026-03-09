package com.scriptuotyper.dto.ranking;

public record GroupRankingResponse(
        int rank,
        Long affiliationId,
        String affiliationName,
        int totalCompletedChapters,
        int memberCount
) {
}
