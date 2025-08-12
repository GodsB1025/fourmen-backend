package com.fourmen.meetingplatform.domain.company.dto.request;

import com.fourmen.meetingplatform.domain.user.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateRoleRequest {

    @NotNull(message = "역할은 필수 입력 값입니다.")
    private Role role;
}