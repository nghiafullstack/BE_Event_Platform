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
public class UserContactResponse {
    private Long userId;
    private Long tenantId;
    private String username;
    private String fullName;
    private String email;
}
