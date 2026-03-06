package com.scriptuotyper.service;

import com.scriptuotyper.domain.user.User;
import com.scriptuotyper.dto.ranking.RankingEntryResponse;
import com.scriptuotyper.dto.ranking.UserTypingCount;
import com.scriptuotyper.repository.ProgressRepository;
import com.scriptuotyper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {

    private static final String RANKING_KEY = "ranking:typing";

    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;
    private final ProgressRepository progressRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void warmUpRanking() {
        Boolean exists = redisTemplate.hasKey(RANKING_KEY);
        if (exists != null && exists) {
            log.info("ranking:typing ZSET already exists, skipping warmup");
            return;
        }

        List<UserTypingCount> counts = progressRepository.sumTypingReadCountByActiveUser();
        if (counts.isEmpty()) {
            log.info("No typing progress data found, skipping ranking warmup");
            return;
        }

        for (UserTypingCount count : counts) {
            redisTemplate.opsForZSet().add(RANKING_KEY, String.valueOf(count.userId()), count.totalCount().doubleValue());
        }
        log.info("Ranking warmup completed: {} users loaded", counts.size());
    }

    public List<RankingEntryResponse> getTopRanking(int limit) {
        Set<TypedTuple<String>> ranked = redisTemplate.opsForZSet()
                .reverseRangeWithScores(RANKING_KEY, 0, limit - 1);

        if (ranked == null || ranked.isEmpty()) {
            return List.of();
        }

        // userId 목록 추출
        List<Long> userIds = ranked.stream()
                .filter(t -> t.getValue() != null)
                .map(t -> Long.parseLong(t.getValue()))
                .toList();

        // 벌크 조회
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<RankingEntryResponse> result = new ArrayList<>();
        int rank = 1;
        for (TypedTuple<String> tuple : ranked) {
            if (tuple.getValue() == null) continue;
            Long userId = Long.parseLong(tuple.getValue());
            int score = tuple.getScore() != null ? tuple.getScore().intValue() : 0;
            User user = userMap.get(userId);
            String name = user != null ? user.getName() : "알 수 없음";
            result.add(new RankingEntryResponse(rank++, userId, name, score));
        }

        return result;
    }
}
