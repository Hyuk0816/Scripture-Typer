package com.scriptuotyper.controller;

import com.scriptuotyper.domain.affiliation.MainAffiliation;
import com.scriptuotyper.domain.user.UserStatus;
import com.scriptuotyper.dto.admin.UserListResponse;
import com.scriptuotyper.dto.affiliation.AffiliationResponse;
import com.scriptuotyper.service.AdminService;
import com.scriptuotyper.service.AffiliationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final AffiliationService affiliationService;

    @PatchMapping("/users/{id}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activateUser(@PathVariable Long id) {
        adminService.activateUser(id);
    }

    @PatchMapping("/users/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateUser(@PathVariable Long id) {
        adminService.deactivateUser(id);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserListResponse>> getUsers(
            @RequestParam(required = false) UserStatus status) {
        List<UserListResponse> users = (status != null)
                ? adminService.getUsersByStatus(status)
                : adminService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/users/{id}/affiliation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateUserAffiliation(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        adminService.updateUserAffiliation(id, body.get("affiliationId"));
    }

    // --- Affiliation CRUD (admin only) ---

    @GetMapping("/affiliations")
    public List<AffiliationResponse> getAffiliations() {
        return affiliationService.getAllAffiliations();
    }

    @PostMapping("/affiliations")
    @ResponseStatus(HttpStatus.CREATED)
    public AffiliationResponse createAffiliation(@RequestBody Map<String, String> body) {
        MainAffiliation main = MainAffiliation.valueOf(body.get("mainAffiliation"));
        String sub = body.get("subAffiliation");
        String displayName = body.get("displayName");
        return affiliationService.createAffiliation(main, sub, displayName);
    }

    @DeleteMapping("/affiliations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAffiliation(@PathVariable Long id) {
        affiliationService.deleteAffiliation(id);
    }
}