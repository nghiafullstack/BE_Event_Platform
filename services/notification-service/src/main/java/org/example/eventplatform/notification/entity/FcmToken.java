package org.example.eventplatform.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.eventplatform.shared.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "fcm_tokens",
        uniqueConstraints = @UniqueConstraint(name = "uk_fcm_token", columnNames = "token"),
        indexes = @Index(name = "idx_fcm_tokens_user_id", columnList = "user_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FcmToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 512)
    private String token;

    /** Last successful register or push touch — used by stale-token cleanup. */
    @Column(name = "last_seen_at", nullable = false)
    private LocalDateTime lastSeenAt;
}
