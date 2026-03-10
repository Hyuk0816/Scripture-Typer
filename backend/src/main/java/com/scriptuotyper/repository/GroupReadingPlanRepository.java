package com.scriptuotyper.repository;

import com.scriptuotyper.domain.group.GroupPlanStatus;
import com.scriptuotyper.domain.group.GroupReadingPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupReadingPlanRepository extends JpaRepository<GroupReadingPlan, Long> {

    List<GroupReadingPlan> findByAffiliationIdAndStatusOrderByCreatedAtDesc(Long affiliationId, GroupPlanStatus status);

    List<GroupReadingPlan> findByAffiliationIdOrderByCreatedAtDesc(Long affiliationId);

    // getPlanDetail()용 - 계획 + 소속 + 생성자 한번에 로드
    @Query("""
            SELECT p FROM GroupReadingPlan p
            LEFT JOIN FETCH p.affiliation
            LEFT JOIN FETCH p.createdBy
            WHERE p.id = :id
            """)
    Optional<GroupReadingPlan> findByIdWithDetails(@Param("id") Long id);
}
