package org.example.eventplatform.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.eventplatform.notification.dto.NotificationResponse;
import org.example.eventplatform.notification.entity.UserNotification;
import org.example.eventplatform.notification.repository.UserNotificationRepository;
import org.example.eventplatform.shared.messaging.NotificationMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class InboxNotificationService {

    private static final Pattern JSON_ENTRY = Pattern.compile("\"((?:\\\\.|[^\"\\\\])*)\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");

    private final UserNotificationRepository userNotificationRepository;

    @Transactional
    public void saveIfAbsent(Long userId, NotificationMessage message) {
        if (userId == null || message.id() == null) {
            return;
        }
        if (userNotificationRepository.existsByMessageIdAndUserId(message.id(), userId)) {
            return;
        }
        userNotificationRepository.save(UserNotification.builder()
                .messageId(message.id())
                .userId(userId)
                .tenantId(message.tenantId())
                .type(message.type())
                .title(message.title())
                .body(message.body())
                .dataJson(toJson(message.data()))
                .build());
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> listForUser(Long userId, Pageable pageable) {
        return userNotificationRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return userNotificationRepository.countByUserIdAndReadAtIsNullAndDeletedFalse(userId);
    }

    @Transactional
    public void markRead(Long userId, Long notificationId) {
        userNotificationRepository.markRead(notificationId, userId, LocalDateTime.now());
    }

    @Transactional
    public void markAllRead(Long userId) {
        userNotificationRepository.markAllRead(userId, LocalDateTime.now());
    }

    private NotificationResponse toResponse(UserNotification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .messageId(n.getMessageId())
                .type(n.getType())
                .title(n.getTitle())
                .body(n.getBody())
                .data(parseData(n.getDataJson()))
                .read(n.getReadAt() != null)
                .readAt(n.getReadAt())
                .createdAt(n.getCreatedAt())
                .build();
    }

    private String toJson(Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> e : data.entrySet()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append('"').append(escape(e.getKey())).append('"')
                    .append(':')
                    .append('"').append(escape(e.getValue() == null ? "" : e.getValue())).append('"');
        }
        return sb.append('}').toString();
    }

    private Map<String, String> parseData(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        Map<String, String> result = new LinkedHashMap<>();
        Matcher matcher = JSON_ENTRY.matcher(json);
        while (matcher.find()) {
            result.put(unescape(matcher.group(1)), unescape(matcher.group(2)));
        }
        return result;
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String unescape(String value) {
        return value.replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
