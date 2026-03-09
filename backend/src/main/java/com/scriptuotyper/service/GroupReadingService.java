package com.scriptuotyper.service;

import com.scriptuotyper.common.exception.user.UserNotFoundException;
import com.scriptuotyper.domain.group.GroupReadingPlan;
import com.scriptuotyper.domain.group.GroupPlanStatus;
import com.scriptuotyper.domain.user.User;
import com.scriptuotyper.dto.group.GroupMemberProgressResponse;
import com.scriptuotyper.dto.group.GroupPlanDetailResponse;
import com.scriptuotyper.dto.group.GroupPlanRequest;
import com.scriptuotyper.dto.group.GroupPlanResponse;
import com.scriptuotyper.repository.BibleRepository;
import com.scriptuotyper.repository.GroupReadingPlanRepository;
import com.scriptuotyper.repository.ProgressRepository;
import com.scriptuotyper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupReadingService {

    private final GroupReadingPlanRepository groupPlanRepository;
    private final UserRepository userRepository;
    private final ProgressRepository progressRepository;
    private final BibleRepository bibleRepository;

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

        return GroupPlanResponse.from(groupPlanRepository.save(plan));
    }

    @Transactional(readOnly = true)
    public List<GroupPlanResponse> getMyGroupPlans(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (user.getAffiliation() == null) {
            return List.of();
        }

        return groupPlanRepository.findByAffiliationIdOrderByCreatedAtDesc(user.getAffiliation().getId())
                .stream()
                .map(GroupPlanResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public GroupPlanDetailResponse getPlanDetail(Long planId) {
        GroupReadingPlan plan = groupPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("그룹 계획을 찾을 수 없습니다"));

        int totalChapters = plan.getEndChapter() - plan.getStartChapter() + 1;

        // Get all members in this affiliation
        List<User> members = userRepository.findByAffiliationId(plan.getAffiliation().getId());
        List<Long> memberIds = members.stream().map(User::getId).toList();

        List<GroupMemberProgressResponse> memberProgress = new ArrayList<>();

        if (!memberIds.isEmpty()) {
            List<Object[]> results = progressRepository.countGroupProgressByBook(
                    plan.getMode(), plan.getBookName(), plan.getStartChapter(), plan.getEndChapter(), memberIds);

            // Map results
            for (Object[] row : results) {
                Long uid = (Long) row[0];
                String name = (String) row[1];
                Long completedChapters = (Long) row[2];
                Long totalReadCount = (Long) row[3];
                memberProgress.add(new GroupMemberProgressResponse(
                        uid, name, completedChapters.intValue(), totalReadCount.intValue()));
            }

            // Add members with no progress
            List<Long> foundUserIds = memberProgress.stream()
                    .map(GroupMemberProgressResponse::userId)
                    .toList();
            for (User member : members) {
                if (!foundUserIds.contains(member.getId())) {
                    memberProgress.add(new GroupMemberProgressResponse(
                            member.getId(), member.getName(), 0, 0));
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
}
