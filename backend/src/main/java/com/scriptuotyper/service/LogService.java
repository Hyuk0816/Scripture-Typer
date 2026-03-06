package com.scriptuotyper.service;

import com.scriptuotyper.domain.log.UserMenuAccessLog;
import com.scriptuotyper.repository.UserMenuAccessLogRepository;
import com.scriptuotyper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogService {

    private static final Logger apiAccessLogger = LoggerFactory.getLogger("API_ACCESS");

    private final UserMenuAccessLogRepository userMenuAccessLogRepository;
    private final UserRepository userRepository;

    @Async
    @Transactional
    public void logMenuAccess(Long userId, String menuName, String path, String ipAddress) {
        apiAccessLogger.info("{} menu-access {} {}", userId, menuName, path);

        UserMenuAccessLog log = UserMenuAccessLog.builder()
                .user(userRepository.getReferenceById(userId))
                .menuName(menuName)
                .path(path)
                .ipAddress(ipAddress)
                .build();
        userMenuAccessLogRepository.save(log);
    }
}
