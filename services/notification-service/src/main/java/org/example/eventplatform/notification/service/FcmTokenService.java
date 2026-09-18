package org.example.eventplatform.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * FCM tokens are per-device, short-lived, and rotate often — a Redis set is
 * enough, no need for the dedicated relational DB the plan otherwise gives
 * every service.
 */
@Service
@RequiredArgsConstructor
public class FcmTokenService {

    private final StringRedisTemplate redisTemplate;

    public void registerToken(Long userId, String token) {
        redisTemplate.opsForSet().add(key(userId), token);
    }

    public Set<String> getTokens(Long userId) {
        Set<String> tokens = redisTemplate.opsForSet().members(key(userId));
        return tokens != null ? tokens : Set.of();
    }

    private String key(Long userId) {
        return "fcm:user:" + userId;
    }
}
