package org.example.eventplatform.identity.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSummaryResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private Long tenantId;
    private String tenantName;
    private String roleName;
    private Set<String> authorities;
}
