package com.fourmen.meetingplatform.domain.company.dto.request;

import com.fourmen.meetingplatform.domain.user.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class AddMemberRequest {

    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email
    private String email;

    @NotNull(message = "역할은 필수 입력 값입니다.")
    private Role role;

}
