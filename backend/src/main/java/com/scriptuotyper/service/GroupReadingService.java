package com.scriptuotyper.service;

import com.scriptuotyper.common.exception.group.GroupInviteNotFoundException;
import com.scriptuotyper.common.exception.group.GroupPlanNotFoundException;
import com.scriptuotyper.common.exception.group.InvalidChapterRangeException;
import com.scriptuotyper.common.exception.group.UserHasNoAffiliationException;
import com.scriptuotyper.common.exception.user.UserNotFoundException;
import com.scriptuotyper.domain.group.GroupInviteStatus;
import com.scriptuotyper.domain.group.GroupPlanMember;
import com.scriptuotyper.domain.group.GroupReadingPlan;
import com.scriptuotyper.domain.user.User;
import com.scriptuotyper.dto.group.*;
import com.scriptuotyper.repository.GroupPlanMemberRepository;
import com.scriptuotyper.repository.GroupReadingPlanRepository;
import com.scriptuotyper.repository.ProgressRepository;
import com.scriptuotyper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupReadingService {

    private final GroupReadingPlanRepository groupPlanRepository;
    private final GroupPlanMemberRepository groupPlanMemberRepository;
    private final UserRepository userRepository;
    private final ProgressRepository progressRepository;

    @Transactional
    public GroupPlanResponse createPlan(Long userId, GroupPlanRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (user.getAffiliation() == null) {
            throw new UserHasNoAffiliationException();
        }

        if (request.getStartChapter() > request.getEndChapter()) {
            throw new InvalidChapterRangeException();
        }

        GroupReadingPlan plan = GroupReadingPlan.builder()
                .affiliation(user.getAffiliation())
                .bookName(request.getBookName())
                .startChapter(request.getStartChapter())
                .endChapter(request.getEndChapter())
                .mode(request.getMode())
                .createdBy(user)
                .build();
        groupPlanRepository.save(plan);

        // Creator is auto-accepted
        GroupPlanMember creatorMember = GroupPlanMember.builder()
                .plan(plan)
                .user(user)
                .status(GroupInviteStatus.ACCEPTED)
                .build();

        if (request.getMemberAssignments() != null && !request.getMemberAssignments().isEmpty()) {
            // memberAssignments 모드: 멤버별 장 범위 지정
            Map<Long, MemberChapterAssignment> assignmentMap = request.getMemberAssignments().stream()
                    .collect(Collectors.toMap(MemberChapterAssignment::userId, Function.identity()));

            // 생성자에게도 범위 할당이 있으면 적용
            MemberChapterAssignment creatorAssignment = assignmentMap.get(userId);
            if (creatorAssignment != null) {
                validateAssignmentRange(creatorAssignment, request.getStartChapter(), request.getEndChapter());
                creatorMember.assignChapterRange(creatorAssignment.startChapter(), creatorAssignment.endChapter());
            }
            groupPlanMemberRepository.save(creatorMember);

            for (MemberChapterAssignment assignment : request.getMemberAssignments()) {
                if (assignment.userId().equals(userId)) continue;
                validateAssignmentRange(assignment, request.getStartChapter(), request.getEndChapter());
                User member = userRepository.findById(assignment.userId()).orElse(null);
                if (member != null) {
                    GroupPlanMember planMember = GroupPlanMember.builder()
                            .plan(plan)
                            .user(member)
                            .status(GroupInviteStatus.PENDING)
                            .build();
                    planMember.assignChapterRange(assignment.startChapter(), assignment.endChapter());
                    groupPlanMemberRepository.save(planMember);
                }
            }
        } else if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            // 레거시 모드: memberIds만 사용
            groupPlanMemberRepository.save(creatorMember);
            for (Long memberId : request.getMemberIds()) {
                if (memberId.equals(userId)) continue;
                User member = userRepository.findById(memberId).orElse(null);
                if (member != null) {
                    groupPlanMemberRepository.save(GroupPlanMember.builder()
                            .plan(plan)
                            .user(member)
                            .status(GroupInviteStatus.PENDING)
                            .build());
                }
            }
        } else {
            groupPlanMemberRepository.save(creatorMember);
        }

        return GroupPlanResponse.from(plan);
    }

    private void validateAssignmentRange(MemberChapterAssignment assignment, int planStart, int planEnd) {
        if (assignment.startChapter() > assignment.endChapter()) {
            throw new InvalidChapterRangeException();
        }
        if (assignment.startChapter() < planStart || assignment.endChapter() > planEnd) {
            throw new InvalidChapterRangeException();
        }
    }

    @Transactional(readOnly = true)
    public List<GroupPlanResponse> getMyGroupPlans(Long userId) {
        User user = userRepository.findByIdWithAffiliation(userId)
                .orElseThrow(UserNotFoundException::new);

        if (user.getAffiliation() == null) {
            return List.of();
        }

        // Return plans where user is a member (accepted) - fetch join to avoid N+1
        List<GroupPlanMember> memberships = groupPlanMemberRepository.findByUserIdAndStatusWithPlan(userId, GroupInviteStatus.ACCEPTED);
        return memberships.stream()
                .map(m -> GroupPlanResponse.from(m.getPlan()))
                .toList();
    }

    @Transactional(readOnly = true)
    public GroupPlanDetailResponse getPlanDetail(Long planId) {
        GroupReadingPlan plan = groupPlanRepository.findByIdWithDetails(planId)
                .orElseThrow(GroupPlanNotFoundException::new);

        int totalChapters = plan.getEndChapter() - plan.getStartChapter() + 1;

        // Get accepted members only - fetch join to avoid N+1
        List<GroupPlanMember> acceptedMembers = groupPlanMemberRepository.findByPlanIdAndStatusWithUser(planId, GroupInviteStatus.ACCEPTED);

        // 멤버별 장 범위 할당 여부 체크
        boolean hasAssignments = acceptedMembers.stream()
                .anyMatch(m -> m.getAssignedStartChapter() != null);

        List<GroupMemberProgressResponse> memberProgress = new ArrayList<>();

        if (hasAssignments) {
            // 멤버별 개별 범위 → 개별 쿼리
            for (GroupPlanMember member : acceptedMembers) {
                int startCh = member.getEffectiveStartChapter();
                int endCh = member.getEffectiveEndChapter();
                long completed = progressRepository.countCompletedChaptersForUser(
                        plan.getMode(), plan.getBookName(), startCh, endCh, member.getUser().getId());
                long readCount = progressRepository.sumReadCountForUser(
                        plan.getMode(), plan.getBookName(), startCh, endCh, member.getUser().getId());
                memberProgress.add(new GroupMemberProgressResponse(
                        member.getUser().getId(),
                        member.getUser().getName(),
                        (int) completed,
                        (int) readCount,
                        member.getAssignedStartChapter(),
                        member.getAssignedEndChapter(),
                        member.getAssignedTotalChapters()));
            }
        } else {
            // 기존 bulk 쿼리 로직
            List<Long> memberIds = acceptedMembers.stream().map(m -> m.getUser().getId()).toList();
            if (!memberIds.isEmpty()) {
                List<Object[]> results = progressRepository.countGroupProgressByBook(
                        plan.getMode(), plan.getBookName(), plan.getStartChapter(), plan.getEndChapter(), memberIds);

                for (Object[] row : results) {
                    Long uid = (Long) row[0];
                    String name = (String) row[1];
                    Long completedChapters = (Long) row[2];
                    Long totalReadCount = (Long) row[3];
                    memberProgress.add(new GroupMemberProgressResponse(
                            uid, name, completedChapters.intValue(), totalReadCount.intValue()));
                }

                // Add accepted members with no progress
                List<Long> foundUserIds = memberProgress.stream()
                        .map(GroupMemberProgressResponse::userId)
                        .toList();
                for (GroupPlanMember member : acceptedMembers) {
                    if (!foundUserIds.contains(member.getUser().getId())) {
                        memberProgress.add(new GroupMemberProgressResponse(
                                member.getUser().getId(), member.getUser().getName(), 0, 0));
                    }
                }
            }
        }

        return GroupPlanDetailResponse.from(plan, totalChapters, memberProgress);
    }

    @Transactional
    public void completePlan(Long planId) {
        GroupReadingPlan plan = groupPlanRepository.findById(planId)
                .orElseThrow(() -> new GroupPlanNotFoundException());
        plan.complete();
    }

    @Transactional
    public void acceptInvite(Long planId, Long userId) {
        GroupPlanMember member = groupPlanMemberRepository.findByPlanIdAndUserId(planId, userId)
                .orElseThrow(() -> new GroupInviteNotFoundException());
        member.accept();
    }

    @Transactional
    public void declineInvite(Long planId, Long userId) {
        GroupPlanMember member = groupPlanMemberRepository.findByPlanIdAndUserId(planId, userId)
                .orElseThrow(() -> new GroupInviteNotFoundException());
        member.decline();
    }

    @Transactional(readOnly = true)
    public List<GroupInviteResponse> getMyPendingInvites(Long userId) {
        return groupPlanMemberRepository.findByUserIdAndStatusWithPlanAndCreator(userId, GroupInviteStatus.PENDING)
                .stream()
                .map(GroupInviteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getPendingInviteCount(Long userId) {
        return groupPlanMemberRepository.countByUserIdAndStatus(userId, GroupInviteStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<AffiliationMemberResponse> getMyAffiliationMembers(Long userId) {
        User user = userRepository.findByIdWithAffiliation(userId)
                .orElseThrow(UserNotFoundException::new);

        if (user.getAffiliation() == null) {
            log.info("User {} has no affiliation, returning empty member list", userId);
            return List.of();
        }

        Long affiliationId = user.getAffiliation().getId();
        log.info("User {} has affiliationId={}, fetching members", userId, affiliationId);

        List<User> members = userRepository.findActiveByAffiliationId(affiliationId);
        log.info("Found {} active members for affiliationId={}", members.size(), affiliationId);

        return members.stream()
                .filter(u -> !u.getId().equals(userId))
                .map(AffiliationMemberResponse::from)
                .toList();
    }
}
