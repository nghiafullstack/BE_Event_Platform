package org.example.eventplatform.identity.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.example.eventplatform.shared.entity.BaseEntity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;
    private String password;
    private String email;
    private String phone;
    private String fullName;
    private Integer seniority;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "verification_token")
    private String verificationToken;
    private Boolean isActive = false;

    @Column(name = "verification_token_expiry")
    private LocalDateTime verificationTokenExpiry;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_confirm", length = 30)
    private RegistrationStatus statusConfirm = RegistrationStatus.PENDING_VERIFICATION;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status", length = 30)
    @Builder.Default
    private AvailabilityStatus availabilityStatus = AvailabilityStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role roles;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_permissions",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;

    /**
     * Authorities embedded in the JWT at login/refresh time — there is no
     * per-request DB lookup, so this is computed once, here, not via Spring
     * Security's UserDetails machinery.
     */
    public Set<String> resolveAuthorities() {
        Set<String> authorities = new HashSet<>();
        if (this.roles != null) {
            authorities.add("ROLE_" + this.roles.getName());
            if (this.roles.getPermissions() != null) {
                this.roles.getPermissions().forEach(p -> authorities.add(p.getName()));
            }
        }
        if (this.permissions != null) {
            this.permissions.forEach(p -> authorities.add(p.getName()));
        }
        return authorities;
    }
}
