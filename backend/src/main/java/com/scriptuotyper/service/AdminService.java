package com.scriptuotyper.service;

import com.scriptuotyper.common.exception.user.UserNotFoundException;
import com.scriptuotyper.domain.user.User;
import com.scriptuotyper.domain.user.UserStatus;
import com.scriptuotyper.dto.admin.UserListResponse;
import com.scriptuotyper.repository.UserRepository;
import com.scriptuotyper.service.ProgressCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ProgressCacheService progressCacheService;

    @Transactional
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        user.changeStatus(UserStatus.ACTIVE);
    }

    @Transactional
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        user.changeStatus(UserStatus.INACTIVE);
        progressCacheService.removeTypingRanking(userId);
    }

    @Transactional(readOnly = true)
    public List<UserListResponse> getUsersByStatus(UserStatus status) {
        return userRepository.findByStatus(status).stream()
                .map(UserListResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserListResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserListResponse::from)
                .toList();
    }
}