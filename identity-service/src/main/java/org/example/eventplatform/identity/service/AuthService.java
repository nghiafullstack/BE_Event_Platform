package org.example.eventplatform.identity.service;

import lombok.RequiredArgsConstructor;
import org.example.eventplatform.identity.dto.auth.AuthTokenResponse;
import org.example.eventplatform.identity.dto.auth.LoginRequest;
import org.example.eventplatform.identity.dto.auth.UserSummaryResponse;
import org.example.eventplatform.identity.entity.User;
import org.example.eventplatform.identity.repository.UserRepository;
import org.example.eventplatform.shared.security.JwtPrincipal;
import org.example.eventplatform.shared.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public AuthTokenResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Tài khoản hoặc mật khẩu không chính xác"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Tài khoản hoặc mật khẩu không chính xác");
        }
        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new RuntimeException("Tài khoản của bạn đã bị khóa");
        }
        if (user.getTenant() != null && !user.getTenant().isActive()) {
            throw new RuntimeException("Đơn vị (tenant) của bạn đã bị ngừng hoạt động");
        }

        return issueTokens(user);
    }

    @Transactional(readOnly = true)
    public AuthTokenResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new RuntimeException("Refresh token không hợp lệ hoặc đã hết hạn");
        }
        JwtPrincipal claims = jwtTokenProvider.parseToken(refreshToken);
        User user = userRepository.findById(claims.userId())
                .orElseThrow(() -> new RuntimeException("Người dùng không còn tồn tại"));

        String accessToken = jwtTokenProvider.generateToken(toPrincipal(user));
        return AuthTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(toSummary(user))
                .build();
    }

    @Transactional(readOnly = true)
    public UserSummaryResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không còn tồn tại"));
        return toSummary(user);
    }

    private AuthTokenResponse issueTokens(User user) {
        JwtPrincipal principal = toPrincipal(user);
        return AuthTokenResponse.builder()
                .accessToken(jwtTokenProvider.generateToken(principal))
                .refreshToken(jwtTokenProvider.generateRefreshToken(principal))
                .user(toSummary(user))
                .build();
    }

    private JwtPrincipal toPrincipal(User user) {
        Long tenantId = user.getTenant() != null ? user.getTenant().getId() : null;
        return new JwtPrincipal(user.getId(), user.getUsername(), tenantId, List.copyOf(user.resolveAuthorities()));
    }

    private UserSummaryResponse toSummary(User user) {
        return UserSummaryResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .tenantId(user.getTenant() != null ? user.getTenant().getId() : null)
                .tenantName(user.getTenant() != null ? user.getTenant().getName() : null)
                .roleName(user.getRoles() != null ? user.getRoles().getName() : null)
                .authorities(user.resolveAuthorities())
                .build();
    }
}
