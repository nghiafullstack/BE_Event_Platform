package org.example.eventplatform.identity.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantSummaryResponse {
    private Long id;
    private String name;
    private String domain;
    private String email;
    private boolean active;
}
