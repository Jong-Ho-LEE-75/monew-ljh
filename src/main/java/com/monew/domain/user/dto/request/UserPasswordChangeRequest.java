package com.monew.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserPasswordChangeRequest(
    @NotBlank
    String currentPassword,

    @NotBlank
    @Size(min = 6, max = 20)
    String newPassword
) {

}
