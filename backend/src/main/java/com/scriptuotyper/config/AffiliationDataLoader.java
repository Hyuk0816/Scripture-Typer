package com.scriptuotyper.config;

import com.scriptuotyper.domain.affiliation.Affiliation;
import com.scriptuotyper.domain.affiliation.MainAffiliation;
import com.scriptuotyper.repository.AffiliationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AffiliationDataLoader implements CommandLineRunner {

    private final AffiliationRepository affiliationRepository;

    @Override
    public void run(String... args) {
        if (affiliationRepository.count() > 0) {
            log.info("Affiliation data already exists, skipping seed");
            return;
        }

        List<Affiliation> affiliations = List.of(
                // 새가족부
                Affiliation.builder()
                        .mainAffiliation(MainAffiliation.SAEGAJOK)
                        .subAffiliation("새가족 리더")
                        .displayName("새가족부 - 새가족 리더")
                        .build(),
                Affiliation.builder()
                        .mainAffiliation(MainAffiliation.SAEGAJOK)
                        .subAffiliation("새가족")
                        .displayName("새가족부 - 새가족")
                        .build(),
                // 임원단
                Affiliation.builder()
                        .mainAffiliation(MainAffiliation.IMWONDAN)
                        .displayName("임원단")
                        .build(),
                // 목장
                Affiliation.builder()
                        .mainAffiliation(MainAffiliation.MOKJANG)
                        .displayName("목장")
                        .build(),
                // 신혼부부
                Affiliation.builder()
                        .mainAffiliation(MainAffiliation.SINON)
                        .displayName("신혼부부")
                        .build()
        );

        affiliationRepository.saveAll(affiliations);
        log.info("Affiliation seed data loaded: {} entries", affiliations.size());
    }
}
