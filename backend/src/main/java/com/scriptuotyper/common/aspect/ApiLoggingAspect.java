package com.scriptuotyper.common.aspect;

import com.scriptuotyper.domain.log.UserApiLog;
import com.scriptuotyper.repository.UserApiLogRepository;
import com.scriptuotyper.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class ApiLoggingAspect {

    private static final Logger apiAccessLogger = LoggerFactory.getLogger("API_ACCESS");

    private final UserApiLogRepository userApiLogRepository;
    private final UserRepository userRepository;

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restControllerMethods() {}

    @Around("restControllerMethods()")
    public Object logApiAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attrs.getRequest();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String ipAddress = extractIpAddress(request);
        Long userId = extractUserId();

        long startTime = System.currentTimeMillis();
        int status = 200;
        String errorMessage = null;

        try {
            Object result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            status = 500;
            errorMessage = truncate(ex.getMessage(), 1000);
            throw ex;
        } finally {
            long executionTimeMs = System.currentTimeMillis() - startTime;
            String userLabel = userId != null ? String.valueOf(userId) : "anonymous";

            // 파일 로그
            if (errorMessage != null) {
                apiAccessLogger.info("{} request {} {} -> {} ({}ms) ERROR: {}",
                        userLabel, method, uri, status, executionTimeMs, errorMessage);
            } else {
                apiAccessLogger.info("{} request {} {} -> {} ({}ms)",
                        userLabel, method, uri, status, executionTimeMs);
            }

            // DB 저장 (실패해도 원래 응답에 영향 없음)
            try {
                UserApiLog log = UserApiLog.builder()
                        .user(userId != null ? userRepository.getReferenceById(userId) : null)
                        .httpMethod(method)
                        .requestUri(truncate(uri, 500))
                        .responseStatus(status)
                        .executionTimeMs(executionTimeMs)
                        .errorMessage(errorMessage)
                        .ipAddress(ipAddress)
                        .build();
                userApiLogRepository.save(log);
            } catch (Exception dbEx) {
                apiAccessLogger.warn("Failed to save API log to DB: {}", dbEx.getMessage());
            }
        }
    }

    private Long extractUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long id) {
            return id;
        }
        return null;
    }

    private String extractIpAddress(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}
