package org.example.eventplatform.notification.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.eventplatform.notification.client.AdminContact;
import org.example.eventplatform.notification.client.IdentityServiceClient;
import org.example.eventplatform.notification.service.EmailService;
import org.example.eventplatform.notification.service.FcmPushService;
import org.example.eventplatform.notification.service.FcmTokenService;
import org.example.eventplatform.shared.messaging.NotificationMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private static final String TYPE_MEMBER_REJECTED = "MEMBER_REJECTED";

    private final IdentityServiceClient identityServiceClient;
    private final FcmTokenService fcmTokenService;
    private final FcmPushService fcmPushService;
    private final EmailService emailService;

    @RabbitListener(queues = "${notification.queue-name}")
    public void handle(NotificationMessage message) {
        log.info("Received notification {} type={}", message.id(), message.type());

        List<AdminContact> tenantAdmins = List.of();
        List<Long> pushRecipients;

        if (message.recipientUserId() != null) {
            pushRecipients = List.of(message.recipientUserId());
        } else if (message.tenantId() != null) {
            tenantAdmins = identityServiceClient.getTenantAdmins(message.tenantId());
            pushRecipients = tenantAdmins.stream().map(AdminContact::userId).toList();
        } else {
            log.warn("Notification {} has neither recipientUserId nor tenantId — dropping", message.id());
            pushRecipients = List.of();
        }

        Map<String, String> data = message.data() != null ? message.data() : Map.of();
        for (Long userId : pushRecipients) {
            fcmTokenService.getTokens(userId)
                    .forEach(token -> fcmPushService.sendPush(token, message.title(), message.body(), data));
        }

        if (TYPE_MEMBER_REJECTED.equals(message.type())) {
            tenantAdmins.stream()
                    .filter(admin -> admin.email() != null)
                    .forEach(admin -> emailService.sendHtml(admin.email(), message.title(), "<p>" + message.body() + "</p>"));
        }
    }
}
