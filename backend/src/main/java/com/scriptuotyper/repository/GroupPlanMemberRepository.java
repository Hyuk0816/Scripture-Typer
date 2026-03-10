package com.scriptuotyper.repository;

import com.scriptuotyper.domain.group.GroupInviteStatus;
import com.scriptuotyper.domain.group.GroupPlanMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupPlanMemberRepository extends JpaRepository<GroupPlanMember, Long> {

    List<GroupPlanMember> findByPlanIdAndStatus(Long planId, GroupInviteStatus status);

    List<GroupPlanMember> findByUserIdAndStatus(Long userId, GroupInviteStatus status);

    Optional<GroupPlanMember> findByPlanIdAndUserId(Long planId, Long userId);

    long countByUserIdAndStatus(Long userId, GroupInviteStatus status);

    // getMyGroupPlans()용 - 멤버 → 계획 + 계획의 소속/생성자 한번에 로드
    @Query("""
            SELECT m FROM GroupPlanMember m
            JOIN FETCH m.plan p
            LEFT JOIN FETCH p.affiliation
            LEFT JOIN FETCH p.createdBy
            WHERE m.user.id = :userId AND m.status = :status
            """)
    List<GroupPlanMember> findByUserIdAndStatusWithPlan(@Param("userId") Long userId,
                                                        @Param("status") GroupInviteStatus status);

    // getPlanDetail()용 - 계획의 멤버 + 유저 한번에 로드
    @Query("""
            SELECT m FROM GroupPlanMember m
            JOIN FETCH m.user
            WHERE m.plan.id = :planId AND m.status = :status
            """)
    List<GroupPlanMember> findByPlanIdAndStatusWithUser(@Param("planId") Long planId,
                                                        @Param("status") GroupInviteStatus status);

    // getMyPendingInvites()용 - 멤버 → 계획 + 생성자 한번에 로드
    @Query("""
            SELECT m FROM GroupPlanMember m
            JOIN FETCH m.plan p
            LEFT JOIN FETCH p.createdBy
            WHERE m.user.id = :userId AND m.status = :status
            """)
    List<GroupPlanMember> findByUserIdAndStatusWithPlanAndCreator(@Param("userId") Long userId,
                                                                  @Param("status") GroupInviteStatus status);
}
