package com.scriptuotyper.repository;

import com.scriptuotyper.domain.affiliation.Affiliation;
import com.scriptuotyper.domain.affiliation.MainAffiliation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AffiliationRepository extends JpaRepository<Affiliation, Long> {

    List<Affiliation> findByMainAffiliation(MainAffiliation mainAffiliation);
}
