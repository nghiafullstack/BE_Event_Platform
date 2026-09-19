package org.example.eventplatform.event.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AssignMemberRequest {
    @NotNull
    private Long userId;
    private String position;

    // Tuỳ chọn: chọn từ danh mục CrewRole của tenant thay vì gõ tay position.
    // Khi có crewRoleId, EventService dùng tên vị trí trong catalog làm hiển thị mặc định.
    private Long crewRoleId;
}
