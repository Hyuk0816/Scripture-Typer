package com.scriptuotyper.controller;

import com.scriptuotyper.domain.user.UserStatus;
import com.scriptuotyper.dto.admin.UserListResponse;
import com.scriptuotyper.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

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
}