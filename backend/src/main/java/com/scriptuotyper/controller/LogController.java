package com.scriptuotyper.controller;

import com.scriptuotyper.dto.log.MenuAccessRequest;
import com.scriptuotyper.service.LogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/log")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    @PostMapping("/menu-access")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logMenuAccess(@AuthenticationPrincipal Long userId,
                              @Valid @RequestBody MenuAccessRequest request,
                              HttpServletRequest httpRequest) {
        String ipAddress = extractIpAddress(httpRequest);
        logService.logMenuAccess(userId, request.menuName(), request.path(), ipAddress);
    }

    private String extractIpAddress(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
