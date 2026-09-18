package org.example.eventplatform.identity.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.example.eventplatform.shared.entity.BaseEntity;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tenants")
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Tenant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String domain;
    private String email;
    private String logo;
    private boolean active = true;

    @Column(name = "verification_token")
    private String verificationToken;

    @Column(name = "verification_token_expiry")
    private LocalDateTime verificationTokenExpiry;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_confirm", length = 30)
    private RegistrationStatus statusConfirm = RegistrationStatus.PENDING_VERIFICATION;

    @OneToMany(mappedBy = "tenant")
    private List<User> users;
}
