package org.example.eventplatform.event.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "user_event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    // identity-service owns the User row — only the id crosses the boundary.
    @Column(name = "user_id", nullable = false)
    private Long userId;

    private String position;

    @Enumerated(EnumType.STRING)
    private AssignStatus status;

    private String note;
    private LocalDateTime respondedAt;

    private LocalTime checkinAt;
    private LocalTime checkoutAt;
    private String checkinLocation;

    private LocalTime actualConcentrateAt;

    @Column(name = "salary", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal salary = BigDecimal.ZERO;
}
