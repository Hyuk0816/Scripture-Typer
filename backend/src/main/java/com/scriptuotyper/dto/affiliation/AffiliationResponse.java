package com.scriptuotyper.dto.affiliation;

import com.scriptuotyper.domain.affiliation.Affiliation;
import com.scriptuotyper.domain.affiliation.MainAffiliation;

public record AffiliationResponse(
        Long id,
        MainAffiliation mainAffiliation,
        String subAffiliation,
        String displayName
) {
    public static AffiliationResponse from(Affiliation affiliation) {
        return new AffiliationResponse(
                affiliation.getId(),
                affiliation.getMainAffiliation(),
                affiliation.getSubAffiliation(),
                affiliation.getDisplayName()
        );
    }
}
