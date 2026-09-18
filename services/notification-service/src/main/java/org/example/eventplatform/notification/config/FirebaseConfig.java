package org.example.eventplatform.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * FCM_CREDENTIALS_JSON is a placeholder until there is a real Firebase
 * project + a real device token (Phase 7). Without valid credentials this
 * bean is simply absent — FcmPushService falls back to logging instead of
 * calling Firebase, so the rest of the pipeline stays testable now.
 */
@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${fcm.credentials-json:}")
    private String credentialsJson;

    @Bean
    public Optional<FirebaseApp> firebaseApp() {
        if (!StringUtils.hasText(credentialsJson) || credentialsJson.startsWith("changeme")) {
            log.warn("FCM_CREDENTIALS_JSON not set (or still the placeholder) — FCM push will only be logged, not actually sent");
            return Optional.empty();
        }

        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                return Optional.of(FirebaseApp.getInstance());
            }
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8)));
            FirebaseOptions options = FirebaseOptions.builder().setCredentials(credentials).build();
            return Optional.of(FirebaseApp.initializeApp(options));
        } catch (Exception ex) {
            log.error("Failed to initialize Firebase — FCM push will only be logged", ex);
            return Optional.empty();
        }
    }
}
