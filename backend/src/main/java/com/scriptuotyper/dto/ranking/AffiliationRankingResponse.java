package com.scriptuotyper.dto.ranking;

import java.util.List;

public record AffiliationRankingResponse(
        String affiliationName,
        int myRank,
        int myCompletedChapters,
        List<RankingEntryResponse> rankings
) {
}
