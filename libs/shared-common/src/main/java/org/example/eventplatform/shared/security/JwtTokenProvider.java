package org.example.eventplatform.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_REFRESH = "refresh";

    private final SecretKey key;
    private final long expirationMillis;
    private final long refreshExpirationMillis;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                             @Value("${jwt.expiration}") long expirationMillis,
                             @Value("${jwt.refresh-expiration:604800000}") long refreshExpirationMillis) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMillis = expirationMillis;
        this.refreshExpirationMillis = refreshExpirationMillis;
    }

    public String generateToken(JwtPrincipal principal) {
        return buildToken(principal, expirationMillis, null);
    }

    public String generateRefreshToken(JwtPrincipal principal) {
        return buildToken(principal, refreshExpirationMillis, TYPE_REFRESH);
    }

    private String buildToken(JwtPrincipal principal, long ttlMillis, String type) {
        Instant now = Instant.now();
        var builder = Jwts.builder()
                .subject(principal.username())
                .claim("userId", principal.userId())
                .claim("tenantId", principal.tenantId())
                .claim("authorities", principal.authorities())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(ttlMillis)));
        if (type != null) {
            builder.claim(CLAIM_TYPE, type);
        }
        return builder.signWith(key).compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return TYPE_REFRESH.equals(claims.get(CLAIM_TYPE, String.class));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public JwtPrincipal parseToken(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        Long userId = claims.get("userId", Long.class);
        Long tenantId = claims.get("tenantId", Long.class);
        List<String> authorities = claims.get("authorities", List.class);
        return new JwtPrincipal(userId, claims.getSubject(), tenantId, authorities == null ? List.of() : authorities);
    }
}
