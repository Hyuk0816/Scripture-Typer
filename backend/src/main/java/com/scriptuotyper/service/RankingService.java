package com.scriptuotyper.service;

import com.scriptuotyper.domain.user.User;
import com.scriptuotyper.dto.ranking.RankingEntryResponse;
import com.scriptuotyper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankingService {

    private static final String RANKING_KEY = "ranking:typing";

    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;

    public List<RankingEntryResponse> getTopRanking(int limit) {
        Set<TypedTuple<String>> ranked = redisTemplate.opsForZSet()
                .reverseRangeWithScores(RANKING_KEY, 0, limit - 1);

        if (ranked == null || ranked.isEmpty()) {
            return List.of();
        }

        // userId 목록 추출
        List<Long> userIds = ranked.stream()
                .map(t -> Long.parseLong(t.getValue()))
                .toList();

        // 벌크 조회
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<RankingEntryResponse> result = new ArrayList<>();
        int rank = 1;
        for (TypedTuple<String> tuple : ranked) {
            Long userId = Long.parseLong(tuple.getValue());
            int score = tuple.getScore() != null ? tuple.getScore().intValue() : 0;
            User user = userMap.get(userId);
            String name = user != null ? user.getName() : "알 수 없음";
            result.add(new RankingEntryResponse(rank++, userId, name, score));
        }

        return result;
    }
}
