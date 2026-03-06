package com.scriptuotyper.service;

import com.scriptuotyper.domain.progress.ProgressMode;
import com.scriptuotyper.dto.admin.*;
import com.scriptuotyper.repository.ChatMessageRepository;
import com.scriptuotyper.repository.ProgressRepository;
import com.scriptuotyper.repository.UserLoginLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

        List<DailyLoginCount> dailyTotals = userLoginLogRepository.countDailyLogins(start, end);
        List<DailyLoginByUser> dailyByUser = userLoginLogRepository.countDailyLoginsByUser(start, end);

        Map<LocalDate, List<DailyLoginStatResponse.UserLoginDetail>> userMap = dailyByUser.stream()
                .collect(Collectors.groupingBy(
                        DailyLoginByUser::loginDate,
                        LinkedHashMap::new,
                        Collectors.mapping(
                                row -> new DailyLoginStatResponse.UserLoginDetail(
                                        row.userId(), row.userName(), row.cnt()),
                                Collectors.toList()
                        )
                ));

        return dailyTotals.stream()
                .map(row -> new DailyLoginStatResponse(
                        row.loginDate(), row.cnt(),
                        userMap.getOrDefault(row.loginDate(), List.of())))
                .toList();
    }

    public List<DailyProgressStatResponse> getDailyProgressStats(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<DailyProgressByUser> rows = progressRepository.countDailyProgressByUser(start, end);

        Map<LocalDate, Map<Long, DailyProgressStatResponse.UserProgressDetail>> dateUserMap =
                new LinkedHashMap<>();

        for (DailyProgressByUser row : rows) {
            dateUserMap
                    .computeIfAbsent(row.progressDate(), d -> new LinkedHashMap<>())
                    .merge(row.userId(),
                            new DailyProgressStatResponse.UserProgressDetail(
                                    row.userId(), row.userName(),
                                    row.mode() == ProgressMode.READING ? row.totalCount() : 0,
                                    row.mode() == ProgressMode.TYPING ? row.totalCount() : 0),
                            (existing, incoming) -> new DailyProgressStatResponse.UserProgressDetail(
                                    row.userId(), row.userName(),
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

        List<DailyChatByUser> rows = chatMessageRepository.countDailyChatByUser(start, end);

        Map<LocalDate, List<DailyChatStatResponse.UserChatDetail>> dateMap = rows.stream()
                .collect(Collectors.groupingBy(
                        DailyChatByUser::chatDate,
                        LinkedHashMap::new,
                        Collectors.mapping(
                                row -> new DailyChatStatResponse.UserChatDetail(
                                        row.userId(), row.userName(), row.cnt()),
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

        List<DailyLoginByUser> loginByUser = userLoginLogRepository.countDailyLoginsByUser(start, end);
        long activeUsers = loginByUser.stream()
                .map(DailyLoginByUser::userId)
                .distinct()
                .count();

        List<MonthlyProgressCount> progressRows = progressRepository.countMonthlyProgress(start, end);
        long totalReading = 0;
        long totalTyping = 0;
        for (MonthlyProgressCount row : progressRows) {
            if (row.mode() == ProgressMode.READING) totalReading = row.totalCount();
            else if (row.mode() == ProgressMode.TYPING) totalTyping = row.totalCount();
        }

        long totalChat = chatMessageRepository.countMonthlyChatQuestions(start, end);

        return new MonthlyStatResponse(year, month, activeUsers, totalReading, totalTyping, totalChat);
    }
}
