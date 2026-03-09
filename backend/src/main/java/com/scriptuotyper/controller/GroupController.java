package com.scriptuotyper.controller;

import com.scriptuotyper.dto.group.GroupPlanDetailResponse;
import com.scriptuotyper.dto.group.GroupPlanRequest;
import com.scriptuotyper.dto.group.GroupPlanResponse;
import com.scriptuotyper.service.GroupReadingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
