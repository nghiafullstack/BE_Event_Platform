package org.example.eventplatform.identity.dto.tenant;

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
public class TenantRegisterResponse {
    private TenantResponse tenant;
    private String adminUsername;
}
