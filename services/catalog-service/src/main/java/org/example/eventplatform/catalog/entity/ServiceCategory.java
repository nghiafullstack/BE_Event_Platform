package org.example.eventplatform.catalog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.eventplatform.shared.entity.BaseEntity;

@Entity
@Table(name = "service_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    // Slug dùng cho URL/tra cứu, ví dụ "lan-su-rong", "ban-nhac"
    @Column(nullable = false, unique = true)
    private String code;

    private String description;

    @Builder.Default
    private boolean active = true;
}
