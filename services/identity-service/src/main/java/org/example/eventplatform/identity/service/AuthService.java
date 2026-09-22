package org.example.eventplatform.identity.service;

import lombok.RequiredArgsConstructor;
import org.example.eventplatform.identity.dto.auth.AuthTokenResponse;
import org.example.eventplatform.identity.dto.auth.LoginRequest;
import org.example.eventplatform.identity.dto.auth.TenantLookupResponse;
import org.example.eventplatform.identity.dto.auth.UserSummaryResponse;
import org.example.eventplatform.identity.entity.Tenant;
import org.example.eventplatform.identity.entity.User;
import org.example.eventplatform.identity.repository.TenantRepository;
import org.example.eventplatform.identity.repository.UserRepository;
import org.example.eventplatform.shared.security.JwtPrincipal;
import org.example.eventplatform.shared.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public AuthTokenResponse login(LoginRequest request) {
        String domain = request.getTenantDomain() != null ? request.getTenantDomain().trim() : "";
        User user;
        if (!domain.isEmpty()) {
            Tenant tenant = tenantRepository.findByDomain(domain)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đoàn với mã này"));
            user = userRepository.findByTenantIdAndUsername(tenant.getId(), request.getUsername())
                    .orElseThrow(() -> new RuntimeException("Tài khoản hoặc mật khẩu không chính xác"));
        } else {
            user = resolveUserWithoutDomain(request.getUsername());
        }

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

    /**
     * Nhánh bỏ trống mã đoàn. Ưu tiên tài khoản Super Admin (tenant = null); nếu không có,
     * rơi về tra cứu username toàn hệ thống để các bản app cũ (chưa cập nhật gửi tenant_domain)
     * vẫn đăng nhập được — chỉ chấp nhận khi username đó chỉ khớp đúng 1 user duy nhất.
     * TODO: gỡ nhánh tương thích ngược này sau khi toàn bộ client đã cập nhật gửi tenant_domain.
     */
    private User resolveUserWithoutDomain(String username) {
        List<User> matches = userRepository.findAllByUsername(username);
        Optional<User> superAdmin = matches.stream().filter(u -> u.getTenant() == null).findFirst();
        if (superAdmin.isPresent()) {
            return superAdmin.get();
        }
        if (matches.size() == 1) {
            return matches.get(0);
        }
        if (matches.size() > 1) {
            throw new RuntimeException("Có nhiều đoàn cùng dùng tài khoản này, vui lòng nhập mã đoàn để đăng nhập");
        }
        throw new RuntimeException("Tài khoản hoặc mật khẩu không chính xác");
    }

    @Transactional(readOnly = true)
    public TenantLookupResponse lookupTenant(String domain) {
        Tenant tenant = tenantRepository.findByDomain(domain.trim())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đoàn với mã này"));
        return TenantLookupResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .logo(tenant.getLogo())
                .active(tenant.isActive())
                .build();
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
                .commissionRate(user.getCommissionRate())
                .build();
    }
}
