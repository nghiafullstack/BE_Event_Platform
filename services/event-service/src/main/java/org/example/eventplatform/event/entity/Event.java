package org.example.eventplatform.event.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.eventplatform.shared.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private EventType type;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;

    @Column(columnDefinition = "TEXT")
    private String location;

    // customer-service and identity-service own these rows — database-per-service,
    // so only the foreign id crosses the boundary, never a JPA relation.
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "tenant_id")
    private Long tenantId;

    private BigDecimal totalAmount;
    private BigDecimal platformFee;

    private LocalTime concentrateTime;
    private String concentrateLocation;

    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserEvent> assignedMembers = new ArrayList<>();
}
