package com.scriptuotyper.controller;

import com.scriptuotyper.dto.group.*;
import com.scriptuotyper.service.GroupReadingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupReadingService groupReadingService;

    @PostMapping("/plans")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupPlanResponse createPlan(
            Authentication authentication,
            @Valid @RequestBody GroupPlanRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return groupReadingService.createPlan(userId, request);
    }

    @GetMapping("/plans")
    public List<GroupPlanResponse> getMyGroupPlans(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return groupReadingService.getMyGroupPlans(userId);
    }

    @GetMapping("/plans/{id}")
    public GroupPlanDetailResponse getPlanDetail(@PathVariable Long id) {
        return groupReadingService.getPlanDetail(id);
    }

    @PatchMapping("/plans/{id}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void completePlan(@PathVariable Long id) {
        groupReadingService.completePlan(id);
    }

    @PostMapping("/plans/{id}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void acceptInvite(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        groupReadingService.acceptInvite(id, userId);
    }

    @PostMapping("/plans/{id}/decline")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void declineInvite(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        groupReadingService.declineInvite(id, userId);
    }

    @GetMapping("/invites/pending")
    public List<GroupInviteResponse> getMyPendingInvites(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return groupReadingService.getMyPendingInvites(userId);
    }

    @GetMapping("/invites/pending/count")
    public Map<String, Long> getPendingInviteCount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Map.of("count", groupReadingService.getPendingInviteCount(userId));
    }

    @GetMapping("/members")
    public List<AffiliationMemberResponse> getMyAffiliationMembers(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return groupReadingService.getMyAffiliationMembers(userId);
    }
}
