package org.example.eventplatform.identity.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.eventplatform.identity.entity.AvailabilityStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAvailabilityRequest {
    @NotNull(message = "availabilityStatus không được để trống")
    private AvailabilityStatus availabilityStatus;
}
