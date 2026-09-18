package org.example.eventplatform.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.eventplatform.shared.messaging.NotificationMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * notification-service (Phase 5) is the consumer; until then these messages
 * just queue up in RabbitMQ. Publish failures are logged, not fatal — a show
 * update should not fail because a push notification couldn't be queued.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${notification.queue-name}")
    private String queueName;

    public void publish(String type, Long recipientUserId, Long tenantId, String title, String body, Map<String, String> data) {
        NotificationMessage message = NotificationMessage.of(type, recipientUserId, tenantId, title, body, data);
        try {
            rabbitTemplate.convertAndSend(queueName, message);
        } catch (Exception ex) {
            log.error("Could not publish notification {} to {}", type, queueName, ex);
        }
    }
}
