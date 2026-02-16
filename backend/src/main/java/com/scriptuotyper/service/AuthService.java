package com.scriptuotyper.service;

import com.scriptuotyper.common.exception.auth.AccountNotApprovedException;
import com.scriptuotyper.common.exception.auth.DuplicateEmailException;
import com.scriptuotyper.common.exception.auth.InvalidCredentialsException;
import com.scriptuotyper.common.exception.auth.InvalidTokenException;
import com.scriptuotyper.common.exception.user.UserNotFoundException;
import com.scriptuotyper.domain.user.User;
import com.scriptuotyper.domain.user.UserStatus;
import com.scriptuotyper.dto.auth.LoginRequest;
import com.scriptuotyper.dto.auth.SignupRequest;
import com.scriptuotyper.dto.auth.TokenResponse;
import com.scriptuotyper.repository.UserRepository;
import com.scriptuotyper.security.JwtTokenProvider;
import com.scriptuotyper.security.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException();
        }

        User user = User.builder()
                .name(request.getName())
                .ttorae(request.getTtorae())
                .phone(request.getPhone())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountNotApprovedException();
        }

        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        refreshTokenService.save(user.getId(), refreshToken);

        return new TokenResponse(accessToken, refreshToken);
    }

    public void logout(Long userId) {
        refreshTokenService.delete(userId);
    }

    @Transactional(readOnly = true)
    public TokenResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new InvalidTokenException();
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);

        if (!refreshTokenService.validate(userId, refreshToken)) {
            throw new InvalidTokenException();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        String newAccessToken = jwtTokenProvider.createAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        refreshTokenService.save(userId, newRefreshToken);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }
}