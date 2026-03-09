package com.scriptuotyper.repository;

import com.scriptuotyper.domain.group.GroupInviteStatus;
import com.scriptuotyper.domain.group.GroupPlanMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupPlanMemberRepository extends JpaRepository<GroupPlanMember, Long> {

    List<GroupPlanMember> findByPlanIdAndStatus(Long planId, GroupInviteStatus status);

    List<GroupPlanMember> findByUserIdAndStatus(Long userId, GroupInviteStatus status);

    Optional<GroupPlanMember> findByPlanIdAndUserId(Long planId, Long userId);

    long countByUserIdAndStatus(Long userId, GroupInviteStatus status);
}
