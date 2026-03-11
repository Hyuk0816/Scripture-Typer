package com.scriptuotyper.service;

import com.scriptuotyper.common.exception.user.InvalidCurrentPasswordException;
import com.scriptuotyper.common.exception.user.PasswordMismatchException;
import com.scriptuotyper.common.exception.user.UserNotFoundException;
import com.scriptuotyper.domain.user.User;
import com.scriptuotyper.dto.user.ChangePasswordRequest;
import com.scriptuotyper.dto.user.UserProfileResponse;
import com.scriptuotyper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(Long userId) {
        User user = userRepository.findByIdWithAffiliation(userId)
                .orElseThrow(UserNotFoundException::new);
        return UserProfileResponse.from(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new PasswordMismatchException();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCurrentPasswordException();
        }

        user.changePassword(passwordEncoder.encode(request.getNewPassword()));
    }
}
