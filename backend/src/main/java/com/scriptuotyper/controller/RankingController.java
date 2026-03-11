package com.scriptuotyper.controller;

import com.scriptuotyper.domain.affiliation.MainAffiliation;
import com.scriptuotyper.domain.progress.ProgressMode;
import com.scriptuotyper.dto.ranking.AffiliationRankingResponse;
import com.scriptuotyper.dto.ranking.GroupRankingResponse;
import com.scriptuotyper.dto.ranking.RankingEntryResponse;
import com.scriptuotyper.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    /**
     * 기존 호환: 필사 전체 랭킹
     */
    @GetMapping("/typing")
    public List<RankingEntryResponse> getTypingRanking(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return rankingService.getTopRanking(Math.min(limit, 50));
    }

    /**
     * 전체 랭킹 (mode 파라미터)
     */
    @GetMapping
    public List<RankingEntryResponse> getRanking(
            @RequestParam ProgressMode mode,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return rankingService.getTopRanking(mode, Math.min(limit, 50));
    }

    /**
     * 내 소속 내 랭킹
     */
    @GetMapping("/my-affiliation")
    public AffiliationRankingResponse getMyAffiliationRanking(
            Authentication authentication,
            @RequestParam ProgressMode mode,
            @RequestParam(defaultValue = "20") int limit
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return rankingService.getMyAffiliationRanking(userId, mode, Math.min(limit, 50));
    }

    /**
     * 월간 랭킹
     */
    @GetMapping("/monthly")
    public List<RankingEntryResponse> getMonthlyRanking(
            @RequestParam ProgressMode mode,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "20") int limit
    ) {
        YearMonth requested = YearMonth.of(year, month);
        YearMonth now = YearMonth.now();
        if (!requested.equals(now) && !requested.equals(now.minusMonths(1))) {
            throw new IllegalArgumentException("Only current or previous month is allowed");
        }
        return rankingService.getMonthlyRanking(mode, year, month, Math.min(limit, 50));
    }

    /**
     * 사랑방 간 랭킹 (그룹 집계, 사랑방 소속만 조회 가능)
     */
    @GetMapping("/sarangbang")
    public List<GroupRankingResponse> getSarangbangRanking(
            Authentication authentication,
            @RequestParam ProgressMode mode
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return rankingService.getSarangbangRankingForUser(userId, mode);
    }

    /**
     * 특정 메인 소속 내 랭킹
     */
    @GetMapping("/affiliation/{main}")
    public List<RankingEntryResponse> getAffiliationRanking(
            @PathVariable MainAffiliation main,
            @RequestParam ProgressMode mode,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return rankingService.getAffiliationRanking(main, mode, Math.min(limit, 50));
    }
}
