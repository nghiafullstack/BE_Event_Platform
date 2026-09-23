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

/**
 * Khi FCM_CREDENTIALS_JSON chưa được cấu hình, bean này trả null — Spring ghi
 * nhận là NullBean nên chỗ inject {@code Optional<FirebaseApp>} nhận được
 * Optional.empty(), và FcmPushService chỉ ghi log thay vì gọi Firebase.
 *
 * Bean phải trả thẳng FirebaseApp chứ không phải Optional&lt;FirebaseApp&gt;:
 * Spring coi tham số kiểu Optional&lt;T&gt; là "dependency tuỳ chọn kiểu T" nên
 * nó đi tìm bean kiểu FirebaseApp — một bean mang kiểu Optional sẽ không bao
 * giờ khớp, và FcmPushService luôn nhận Optional.empty() dù credentials đúng.
 */
@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${fcm.credentials-json:}")
    private String credentialsJson;

    @Bean
    public FirebaseApp firebaseApp() {
        if (!StringUtils.hasText(credentialsJson) || credentialsJson.startsWith("changeme")) {
            log.warn("FCM_CREDENTIALS_JSON not set (or still the placeholder) — FCM push will only be logged, not actually sent");
            return null;
        }

        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                return FirebaseApp.getInstance();
            }
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8)));
            FirebaseOptions options = FirebaseOptions.builder().setCredentials(credentials).build();
            FirebaseApp app = FirebaseApp.initializeApp(options);
            log.info("Firebase đã khởi tạo — FCM push sẽ được gửi thật");
            return app;
        } catch (Exception ex) {
            log.error("Failed to initialize Firebase — FCM push will only be logged", ex);
            return null;
        }
    }
}
