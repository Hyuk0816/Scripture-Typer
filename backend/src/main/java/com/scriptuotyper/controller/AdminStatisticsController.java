package com.scriptuotyper.controller;

import com.scriptuotyper.dto.admin.DailyChatStatResponse;
import com.scriptuotyper.dto.admin.DailyLoginStatResponse;
import com.scriptuotyper.dto.admin.DailyProgressStatResponse;
import com.scriptuotyper.dto.admin.MonthlyStatResponse;
import com.scriptuotyper.service.AdminStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class AdminStatisticsController {

    private final AdminStatisticsService adminStatisticsService;

    @GetMapping("/login-daily")
    public ResponseEntity<List<DailyLoginStatResponse>> getDailyLoginStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(adminStatisticsService.getDailyLoginStats(startDate, endDate));
    }

    @GetMapping("/progress-daily")
    public ResponseEntity<List<DailyProgressStatResponse>> getDailyProgressStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(adminStatisticsService.getDailyProgressStats(startDate, endDate));
    }

    @GetMapping("/chat-daily")
    public ResponseEntity<List<DailyChatStatResponse>> getDailyChatStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(adminStatisticsService.getDailyChatStats(startDate, endDate));
    }

    @GetMapping("/monthly")
    public ResponseEntity<MonthlyStatResponse> getMonthlyStats(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(adminStatisticsService.getMonthlyStats(year, month));
    }
}
