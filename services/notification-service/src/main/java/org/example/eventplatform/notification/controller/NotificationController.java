package org.example.eventplatform.notification.controller;

import lombok.RequiredArgsConstructor;
import org.example.eventplatform.notification.dto.NotificationResponse;
import org.example.eventplatform.notification.service.InboxNotificationService;
import org.example.eventplatform.shared.security.JwtPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final InboxNotificationService inboxNotificationService;

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> list(@AuthenticationPrincipal JwtPrincipal principal,
                                                            Pageable pageable) {
        return ResponseEntity.ok(inboxNotificationService.listForUser(principal.userId(), pageable));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(@AuthenticationPrincipal JwtPrincipal principal) {
        return ResponseEntity.ok(Map.of("count", inboxNotificationService.countUnread(principal.userId())));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@AuthenticationPrincipal JwtPrincipal principal,
                                          @PathVariable Long id) {
        inboxNotificationService.markRead(principal.userId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal JwtPrincipal principal) {
        inboxNotificationService.markAllRead(principal.userId());
        return ResponseEntity.noContent().build();
    }
}
