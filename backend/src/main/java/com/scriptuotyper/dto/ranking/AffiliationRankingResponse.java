package com.scriptuotyper.dto.ranking;

import java.util.List;

public record AffiliationRankingResponse(
        String affiliationName,
        String mainAffiliation,
        int myRank,
        int myCompletedChapters,
        List<RankingEntryResponse> rankings
) {
}
