package com.scriptuotyper.controller;

import com.scriptuotyper.dto.ranking.RankingEntryResponse;
import com.scriptuotyper.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/typing")
    public List<RankingEntryResponse> getTypingRanking(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return rankingService.getTopRanking(Math.min(limit, 50));
    }
}
