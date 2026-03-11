package com.scriptuotyper.controller;

import com.scriptuotyper.domain.affiliation.MainAffiliation;
import com.scriptuotyper.dto.affiliation.AffiliationResponse;
import com.scriptuotyper.service.AffiliationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/affiliations")
@RequiredArgsConstructor
public class AffiliationController {

    private final AffiliationService affiliationService;

    @GetMapping
    public List<AffiliationResponse> getAllAffiliations() {
        return affiliationService.getAllAffiliations();
    }

    @GetMapping("/{main}/subs")
    public List<AffiliationResponse> getSubAffiliations(@PathVariable MainAffiliation main) {
        return affiliationService.getSubAffiliations(main);
    }
}
