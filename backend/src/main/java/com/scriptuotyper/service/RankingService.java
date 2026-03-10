package com.scriptuotyper.service;

import com.scriptuotyper.common.exception.user.UserNotFoundException;
import com.scriptuotyper.domain.affiliation.Affiliation;
import com.scriptuotyper.domain.affiliation.MainAffiliation;
import com.scriptuotyper.domain.progress.ProgressMode;
import com.scriptuotyper.domain.user.User;
import com.scriptuotyper.dto.ranking.AffiliationRankingResponse;
import com.scriptuotyper.dto.ranking.GroupRankingResponse;
import com.scriptuotyper.dto.ranking.RankingEntryResponse;
import com.scriptuotyper.dto.ranking.UserTypingCount;
import com.scriptuotyper.repository.AffiliationRepository;
import com.scriptuotyper.repository.ProgressRepository;
import com.scriptuotyper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingService {

    private static final String RANKING_KEY = "ranking:typing";

    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;
    private final ProgressRepository progressRepository;
    private final AffiliationRepository affiliationRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void warmUpRanking() {
        // Warm up typing ranking (legacy)
        warmUpRankingForMode(ProgressMode.TYPING);
        // Warm up reading ranking
        warmUpRankingForMode(ProgressMode.READING);
    }

    private void warmUpRankingForMode(ProgressMode mode) {
        String key = "ranking:" + mode.name();
        Boolean exists = redisTemplate.hasKey(key);
        if (exists != null && exists) {
            log.info("{} ZSET already exists, skipping warmup", key);
            return;
        }

        List<UserTypingCount> counts = progressRepository.sumReadCountByModeForAllUsers(mode);
        if (counts.isEmpty()) {
            log.info("No {} progress data found, skipping ranking warmup", mode);
            return;
        }

        for (UserTypingCount count : counts) {
            redisTemplate.opsForZSet().add(key, String.valueOf(count.userId()), count.totalCount().doubleValue());
        }

        // Also warm up legacy key for typing
        if (mode == ProgressMode.TYPING) {
            Boolean legacyExists = redisTemplate.hasKey(RANKING_KEY);
            if (legacyExists == null || !legacyExists) {
                for (UserTypingCount count : counts) {
                    redisTemplate.opsForZSet().add(RANKING_KEY, String.valueOf(count.userId()), count.totalCount().doubleValue());
                }
            }
        }

        log.info("Ranking warmup completed for {}: {} users loaded", mode, counts.size());
    }

    /**
     * 전체 랭킹 (모드별)
     */
    @Cacheable(value = "ranking:top", key = "#mode + ':' + #limit")
    public List<RankingEntryResponse> getTopRanking(ProgressMode mode, int limit) {
        String key = "ranking:" + mode.name();
        return fetchRankingFromZSet(key, limit);
    }

    /**
     * 기존 호환: typing 전체 랭킹
     */
    @Cacheable(value = "ranking:top", key = "#limit")
    public List<RankingEntryResponse> getTopRanking(int limit) {
        return fetchRankingFromZSet(RANKING_KEY, limit);
    }

    /**
     * 내 소속 내 랭킹
     */
    public AffiliationRankingResponse getMyAffiliationRanking(Long userId, ProgressMode mode, int limit) {
        User user = userRepository.findByIdWithAffiliation(userId)
                .orElseThrow(UserNotFoundException::new);

        if (user.getAffiliation() == null) {
            return new AffiliationRankingResponse("미지정", 0, 0, List.of());
        }

        Long affiliationId = user.getAffiliation().getId();
        String affiliationName = user.getAffiliation().getDisplayName();

        // DB aggregate query (cached)
        List<UserTypingCount> counts = progressRepository.sumReadCountByAffiliationAndMode(affiliationId, mode);

        // Sort descending
        counts.sort((a, b) -> Long.compare(b.totalCount(), a.totalCount()));

        List<Long> userIds = counts.stream().map(UserTypingCount::userId).toList();
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<RankingEntryResponse> rankings = new ArrayList<>();
        int myRank = 0;
        int myChapters = 0;
        int rank = 1;
        for (UserTypingCount count : counts) {
            User u = userMap.get(count.userId());
            String name = u != null ? u.getName() : "알 수 없음";
            rankings.add(new RankingEntryResponse(rank, count.userId(), name, count.totalCount().intValue()));
            if (count.userId().equals(userId)) {
                myRank = rank;
                myChapters = count.totalCount().intValue();
            }
            rank++;
            if (rankings.size() >= limit) break;
        }

        return new AffiliationRankingResponse(affiliationName, myRank, myChapters, rankings);
    }

    /**
     * 사랑방 간 랭킹 (그룹 집계)
     */
    @Cacheable(value = "ranking:sarangbang", key = "#mode")
    public List<GroupRankingResponse> getSarangbangRanking(ProgressMode mode) {
        List<UserTypingCount> groupCounts = progressRepository.sumReadCountGroupByAffiliation(
                MainAffiliation.SARANGBANG, mode);

        // affiliationId → count map
        Map<Long, Long> countMap = groupCounts.stream()
                .collect(Collectors.toMap(UserTypingCount::userId, UserTypingCount::totalCount));

        // Fetch affiliation names
        List<Affiliation> sarangbangAffiliations = affiliationRepository.findByMainAffiliation(MainAffiliation.SARANGBANG);
        Map<Long, Affiliation> affMap = sarangbangAffiliations.stream()
                .collect(Collectors.toMap(Affiliation::getId, Function.identity()));

        List<GroupRankingResponse> result = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : countMap.entrySet()) {
            Long affId = entry.getKey();
            Long total = entry.getValue();
            Affiliation aff = affMap.get(affId);
            String name = aff != null ? aff.getDisplayName() : "알 수 없음";
            int memberCount = (int) userRepository.countByAffiliationId(affId);
            result.add(new GroupRankingResponse(0, affId, name, total.intValue(), memberCount));
        }

        // Sort descending and assign rank
        result.sort((a, b) -> Integer.compare(b.totalCompletedChapters(), a.totalCompletedChapters()));
        List<GroupRankingResponse> ranked = new ArrayList<>();
        int rank = 1;
        for (GroupRankingResponse r : result) {
            ranked.add(new GroupRankingResponse(rank++, r.affiliationId(), r.affiliationName(),
                    r.totalCompletedChapters(), r.memberCount()));
        }

        return ranked;
    }

    /**
     * 특정 메인 소속 내 랭킹
     */
    public List<RankingEntryResponse> getAffiliationRanking(MainAffiliation main, ProgressMode mode, int limit) {
        List<Affiliation> affiliations = affiliationRepository.findByMainAffiliation(main);
        Set<Long> affiliationIds = affiliations.stream().map(Affiliation::getId).collect(Collectors.toSet());

        // Get all users in this main affiliation (single query instead of N loop)
        List<User> users = affiliationIds.isEmpty() ? List.of() : userRepository.findByAffiliationIdIn(affiliationIds);

        Set<Long> userIds = users.stream().map(User::getId).collect(Collectors.toSet());

        // DB aggregate query
        List<UserTypingCount> counts = progressRepository.sumReadCountByModeForAllUsers(mode);
        List<UserTypingCount> filtered = counts.stream()
                .filter(c -> userIds.contains(c.userId()))
                .sorted((a, b) -> Long.compare(b.totalCount(), a.totalCount()))
                .limit(limit)
                .toList();

        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<RankingEntryResponse> result = new ArrayList<>();
        int rank = 1;
        for (UserTypingCount count : filtered) {
            User u = userMap.get(count.userId());
            String name = u != null ? u.getName() : "알 수 없음";
            result.add(new RankingEntryResponse(rank++, count.userId(), name, count.totalCount().intValue()));
        }

        return result;
    }

    // --- Private helpers ---

    private List<RankingEntryResponse> fetchRankingFromZSet(String zsetKey, int limit) {
        Set<TypedTuple<String>> ranked = redisTemplate.opsForZSet()
                .reverseRangeWithScores(zsetKey, 0, limit - 1);

        if (ranked == null || ranked.isEmpty()) {
            return List.of();
        }

        List<Long> userIds = ranked.stream()
                .filter(t -> t.getValue() != null)
                .map(t -> Long.parseLong(t.getValue()))
                .toList();

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
