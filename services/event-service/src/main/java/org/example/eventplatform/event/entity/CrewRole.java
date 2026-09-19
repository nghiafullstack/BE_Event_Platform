package org.example.eventplatform.event.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.eventplatform.shared.entity.BaseEntity;

/**
 * A tenant's own crew position catalog (e.g. department "Múa Lân" → role
 * "Đầu Lân"), used to structure show assignments instead of free-text.
 */
@Entity
@Table(name = "crew_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrewRole extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    private String department;
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;
}
