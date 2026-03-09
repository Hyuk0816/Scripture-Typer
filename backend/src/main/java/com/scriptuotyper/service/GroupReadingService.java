package com.scriptuotyper.service;

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
            throw new IllegalStateException("소속이 지정되지 않은 사용자는 그룹 계획을 생성할 수 없습니다");
        }

        if (request.getStartChapter() > request.getEndChapter()) {
            throw new IllegalArgumentException("시작 장이 끝 장보다 클 수 없습니다");
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
        groupPlanMemberRepository.save(GroupPlanMember.builder()
                .plan(plan)
                .user(user)
                .status(GroupInviteStatus.ACCEPTED)
                .build());

        // Invite selected members (exclude creator if included)
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

        return GroupPlanResponse.from(plan);
    }

    @Transactional(readOnly = true)
    public List<GroupPlanResponse> getMyGroupPlans(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (user.getAffiliation() == null) {
            return List.of();
        }

        // Return plans where user is a member (accepted)
        List<GroupPlanMember> memberships = groupPlanMemberRepository.findByUserIdAndStatus(userId, GroupInviteStatus.ACCEPTED);
        return memberships.stream()
                .map(m -> GroupPlanResponse.from(m.getPlan()))
                .toList();
    }

    @Transactional(readOnly = true)
    public GroupPlanDetailResponse getPlanDetail(Long planId) {
        GroupReadingPlan plan = groupPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("그룹 계획을 찾을 수 없습니다"));

        int totalChapters = plan.getEndChapter() - plan.getStartChapter() + 1;

        // Get accepted members only
        List<GroupPlanMember> acceptedMembers = groupPlanMemberRepository.findByPlanIdAndStatus(planId, GroupInviteStatus.ACCEPTED);
        List<Long> memberIds = acceptedMembers.stream().map(m -> m.getUser().getId()).toList();

        List<GroupMemberProgressResponse> memberProgress = new ArrayList<>();

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

        return GroupPlanDetailResponse.from(plan, totalChapters, memberProgress);
    }

    @Transactional
    public void completePlan(Long planId) {
        GroupReadingPlan plan = groupPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("그룹 계획을 찾을 수 없습니다"));
        plan.complete();
    }

    @Transactional
    public void acceptInvite(Long planId, Long userId) {
        GroupPlanMember member = groupPlanMemberRepository.findByPlanIdAndUserId(planId, userId)
                .orElseThrow(() -> new IllegalArgumentException("초대를 찾을 수 없습니다"));
        member.accept();
    }

    @Transactional
    public void declineInvite(Long planId, Long userId) {
        GroupPlanMember member = groupPlanMemberRepository.findByPlanIdAndUserId(planId, userId)
                .orElseThrow(() -> new IllegalArgumentException("초대를 찾을 수 없습니다"));
        member.decline();
    }

    @Transactional(readOnly = true)
    public List<GroupInviteResponse> getMyPendingInvites(Long userId) {
        return groupPlanMemberRepository.findByUserIdAndStatus(userId, GroupInviteStatus.PENDING)
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
        User user = userRepository.findById(userId)
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
