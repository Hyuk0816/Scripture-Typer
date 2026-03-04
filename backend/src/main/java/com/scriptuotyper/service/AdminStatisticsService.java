package com.scriptuotyper.service;

import com.scriptuotyper.domain.progress.ProgressMode;
import com.scriptuotyper.dto.admin.DailyChatStatResponse;
import com.scriptuotyper.dto.admin.DailyLoginStatResponse;
import com.scriptuotyper.dto.admin.DailyProgressStatResponse;
import com.scriptuotyper.dto.admin.MonthlyStatResponse;
import com.scriptuotyper.repository.ChatMessageRepository;
import com.scriptuotyper.repository.ProgressRepository;
import com.scriptuotyper.repository.UserLoginLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStatisticsService {

    private final UserLoginLogRepository userLoginLogRepository;
    private final ProgressRepository progressRepository;
    private final ChatMessageRepository chatMessageRepository;

    public List<DailyLoginStatResponse> getDailyLoginStats(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<Object[]> dailyTotals = userLoginLogRepository.countDailyLogins(start, end);
        List<Object[]> dailyByUser = userLoginLogRepository.countDailyLoginsByUser(start, end);

        Map<LocalDate, List<DailyLoginStatResponse.UserLoginDetail>> userMap = dailyByUser.stream()
                .collect(Collectors.groupingBy(
                        row -> (LocalDate) row[0],
                        LinkedHashMap::new,
                        Collectors.mapping(
                                row -> new DailyLoginStatResponse.UserLoginDetail(
                                        (Long) row[1], (String) row[2], (Long) row[3]),
                                Collectors.toList()
                        )
                ));

        return dailyTotals.stream()
                .map(row -> {
                    LocalDate date = (LocalDate) row[0];
                    long total = (Long) row[1];
                    List<DailyLoginStatResponse.UserLoginDetail> users =
                            userMap.getOrDefault(date, List.of());
                    return new DailyLoginStatResponse(date, total, users);
                })
                .toList();
    }

    public List<DailyProgressStatResponse> getDailyProgressStats(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<Object[]> rows = progressRepository.countDailyProgressByUser(start, end);

        // Group by (date, userId) and merge READING/TYPING counts
        Map<LocalDate, Map<Long, DailyProgressStatResponse.UserProgressDetail>> dateUserMap =
                new LinkedHashMap<>();

        for (Object[] row : rows) {
            LocalDate date = (LocalDate) row[0];
            Long userId = (Long) row[1];
            String userName = (String) row[2];
            ProgressMode mode = (ProgressMode) row[3];
            long count = (Long) row[4];

            dateUserMap
                    .computeIfAbsent(date, d -> new LinkedHashMap<>())
                    .merge(userId,
                            new DailyProgressStatResponse.UserProgressDetail(
                                    userId, userName,
                                    mode == ProgressMode.READING ? count : 0,
                                    mode == ProgressMode.TYPING ? count : 0),
                            (existing, incoming) -> new DailyProgressStatResponse.UserProgressDetail(
                                    userId, userName,
                                    existing.readingCount() + incoming.readingCount(),
                                    existing.typingCount() + incoming.typingCount()));
        }

        return dateUserMap.entrySet().stream()
                .map(entry -> new DailyProgressStatResponse(
                        entry.getKey(),
                        new ArrayList<>(entry.getValue().values())))
                .toList();
    }

    public List<DailyChatStatResponse> getDailyChatStats(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<Object[]> rows = chatMessageRepository.countDailyChatByUser(start, end);

        Map<LocalDate, List<DailyChatStatResponse.UserChatDetail>> dateMap = rows.stream()
                .collect(Collectors.groupingBy(
                        row -> (LocalDate) row[0],
                        LinkedHashMap::new,
                        Collectors.mapping(
                                row -> new DailyChatStatResponse.UserChatDetail(
                                        (Long) row[1], (String) row[2], (Long) row[3]),
                                Collectors.toList()
                        )
                ));

        return dateMap.entrySet().stream()
                .map(entry -> new DailyChatStatResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    public MonthlyStatResponse getMonthlyStats(int year, int month) {
        LocalDateTime start = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime end = start.plusMonths(1);

        // Active users = distinct users who logged in this month
        List<Object[]> loginByUser = userLoginLogRepository.countDailyLoginsByUser(start, end);
        long activeUsers = loginByUser.stream()
                .map(row -> (Long) row[1])
                .distinct()
                .count();

        // Progress stats
        List<Object[]> progressRows = progressRepository.countMonthlyProgress(start, end);
        long totalReading = 0;
        long totalTyping = 0;
        for (Object[] row : progressRows) {
            ProgressMode mode = (ProgressMode) row[0];
            long count = (Long) row[1];
            if (mode == ProgressMode.READING) totalReading = count;
            else if (mode == ProgressMode.TYPING) totalTyping = count;
        }

        // Chat questions
        long totalChat = chatMessageRepository.countMonthlyChatQuestions(start, end);

        return new MonthlyStatResponse(year, month, activeUsers, totalReading, totalTyping, totalChat);
    }
}
