package com.scriptuotyper.service;

import com.scriptuotyper.common.exception.chat.DailyLimitExceededException;
import com.scriptuotyper.common.exception.user.UserNotFoundException;
import com.scriptuotyper.domain.user.Role;
import com.scriptuotyper.domain.user.User;
import com.scriptuotyper.dto.chat.ChatUsageResponse;
import com.scriptuotyper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiUsageService {

    private static final String KEY_PREFIX = "chat:usage:";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;

    @Value("${gemini.daily-limit}")
    private int dailyLimit;

    public void checkAndIncrement(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (user.getRole() == Role.ADMIN) {
            return;
        }

        String key = buildKey(userId);
        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1L) {
            redisTemplate.expire(key, 86400, TimeUnit.SECONDS);
        }

        if (count != null && count > dailyLimit) {
            redisTemplate.opsForValue().decrement(key);
            throw new DailyLimitExceededException();
        }
    }

    public ChatUsageResponse getUsageToday(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        boolean unlimited = user.getRole() == Role.ADMIN;

        String key = buildKey(userId);
        String value = redisTemplate.opsForValue().get(key);
        int used = value != null ? Integer.parseInt(value) : 0;

        return new ChatUsageResponse(used, dailyLimit, unlimited);
    }

    private String buildKey(Long userId) {
        return KEY_PREFIX + userId + ":" + LocalDate.now().format(DATE_FMT);
    }
}
