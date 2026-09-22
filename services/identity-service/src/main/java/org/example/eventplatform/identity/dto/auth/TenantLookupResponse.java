package org.example.eventplatform.identity.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TenantLookupResponse {
    private Long id;
    private String name;
    private String logo;
    private boolean active;
}
