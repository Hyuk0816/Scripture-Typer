package com.scriptuotyper.service;

import com.scriptuotyper.domain.affiliation.Affiliation;
import com.scriptuotyper.domain.affiliation.MainAffiliation;
import com.scriptuotyper.dto.affiliation.AffiliationResponse;
import com.scriptuotyper.repository.AffiliationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AffiliationService {

    private final AffiliationRepository affiliationRepository;

    @Transactional(readOnly = true)
    public List<AffiliationResponse> getAllAffiliations() {
        return affiliationRepository.findAll().stream()
                .map(AffiliationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AffiliationResponse> getSubAffiliations(MainAffiliation main) {
        return affiliationRepository.findByMainAffiliation(main).stream()
                .map(AffiliationResponse::from)
                .toList();
    }

    @Transactional
    public AffiliationResponse createAffiliation(MainAffiliation mainAffiliation, String subAffiliation, String displayName) {
        Affiliation affiliation = Affiliation.builder()
                .mainAffiliation(mainAffiliation)
                .subAffiliation(subAffiliation)
                .displayName(displayName)
                .build();
        return AffiliationResponse.from(affiliationRepository.save(affiliation));
    }

    @Transactional
    public void deleteAffiliation(Long id) {
        affiliationRepository.deleteById(id);
    }
}
