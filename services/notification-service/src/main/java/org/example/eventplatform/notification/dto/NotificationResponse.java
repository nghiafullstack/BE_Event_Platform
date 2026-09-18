package org.example.eventplatform.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String messageId;
    private String type;
    private String title;
    private String body;
    private Map<String, String> data;
    private boolean read;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
