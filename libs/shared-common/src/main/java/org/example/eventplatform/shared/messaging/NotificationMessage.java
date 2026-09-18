package org.example.eventplatform.shared.messaging;

import java.util.Map;
import java.util.UUID;

/**
 * Message contract published to the notification queue. event-service (and
 * later others) publish these; notification-service (Phase 5) consumes and
 * turns them into FCM push / email. recipientUserId null means "broadcast to
 * every admin of tenantId" rather than one person.
 */
public record NotificationMessage(
        String id,
        String type,
        Long recipientUserId,
        Long tenantId,
        String title,
        String body,
        Map<String, String> data
) {
    public static NotificationMessage of(String type, Long recipientUserId, Long tenantId,
                                          String title, String body, Map<String, String> data) {
        return new NotificationMessage(UUID.randomUUID().toString(), type, recipientUserId, tenantId, title, body, data);
    }
}
