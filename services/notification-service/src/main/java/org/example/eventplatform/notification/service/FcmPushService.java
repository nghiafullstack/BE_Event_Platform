package org.example.eventplatform.notification.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmPushService {

    private final Optional<FirebaseApp> firebaseApp;

    public void sendPush(String token, String title, String body, Map<String, String> data) {
        if (firebaseApp.isEmpty()) {
            log.info("[DEV] would send FCM push to token={} title=\"{}\" body=\"{}\" data={}", token, title, body, data);
            return;
        }

        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .putAllData(data)
                .build();

        try {
            String response = FirebaseMessaging.getInstance(firebaseApp.get()).send(message);
            log.info("Sent FCM push, response={}", response);
        } catch (Exception ex) {
            log.error("Failed to send FCM push to token={}", token, ex);
        }
    }
}
