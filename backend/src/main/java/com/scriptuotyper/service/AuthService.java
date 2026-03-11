package com.scriptuotyper.service;

import com.scriptuotyper.common.exception.auth.AccountNotApprovedException;
import com.scriptuotyper.common.exception.auth.DuplicateEmailException;
import com.scriptuotyper.common.exception.auth.InvalidCredentialsException;
import com.scriptuotyper.common.exception.auth.InvalidTokenException;
import com.scriptuotyper.common.exception.auth.UserIdentityNotFoundException;
import com.scriptuotyper.common.exception.user.PasswordMismatchException;
import com.scriptuotyper.common.exception.user.UserNotFoundException;
import com.scriptuotyper.domain.affiliation.Affiliation;
import com.scriptuotyper.domain.user.User;
import com.scriptuotyper.domain.user.UserLoginLog;
import com.scriptuotyper.domain.user.UserStatus;
import com.scriptuotyper.dto.auth.LoginRequest;
import com.scriptuotyper.dto.auth.ResetPasswordRequest;
import com.scriptuotyper.dto.auth.SignupRequest;
import com.scriptuotyper.dto.auth.TokenResponse;
import com.scriptuotyper.dto.auth.VerifyIdentityRequest;
import com.scriptuotyper.repository.AffiliationRepository;
import com.scriptuotyper.repository.UserLoginLogRepository;
import com.scriptuotyper.repository.UserRepository;
import com.scriptuotyper.security.JwtTokenProvider;
import com.scriptuotyper.security.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserLoginLogRepository userLoginLogRepository;
    private final AffiliationRepository affiliationRepository;
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

        if (request.getAffiliationId() != null) {
            affiliationRepository.findById(request.getAffiliationId())
                    .ifPresent(user::changeAffiliation);
        }

        userRepository.save(user);
    }

    @Transactional
    public TokenResponse login(LoginRequest request, String ipAddress) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountNotApprovedException();
        }

        userLoginLogRepository.save(UserLoginLog.builder()
                .user(user)
                .ipAddress(ipAddress)
                .build());

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

    @Transactional(readOnly = true)
    public void verifyIdentity(VerifyIdentityRequest request) {
        User user = userRepository.findByEmailAndNameAndTtorae(
                        request.getEmail(), request.getName(), request.getTtorae())
                .orElseThrow(UserIdentityNotFoundException::new);

        Long userAffId = user.getAffiliation() != null ? user.getAffiliation().getId() : null;
        if (!Objects.equals(userAffId, request.getAffiliationId())) {
            throw new UserIdentityNotFoundException();
        }
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new PasswordMismatchException();
        }

        User user = userRepository.findByEmailAndNameAndTtorae(
                        request.getEmail(), request.getName(), request.getTtorae())
                .orElseThrow(UserIdentityNotFoundException::new);

        Long userAffId = user.getAffiliation() != null ? user.getAffiliation().getId() : null;
        if (!Objects.equals(userAffId, request.getAffiliationId())) {
            throw new UserIdentityNotFoundException();
        }

        user.changePassword(passwordEncoder.encode(request.getNewPassword()));
    }
}