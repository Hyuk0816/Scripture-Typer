package com.scriptuotyper.repository;

import com.scriptuotyper.domain.group.GroupPlanStatus;
import com.scriptuotyper.domain.group.GroupReadingPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupReadingPlanRepository extends JpaRepository<GroupReadingPlan, Long> {

    List<GroupReadingPlan> findByAffiliationIdAndStatusOrderByCreatedAtDesc(Long affiliationId, GroupPlanStatus status);

    List<GroupReadingPlan> findByAffiliationIdOrderByCreatedAtDesc(Long affiliationId);
}
