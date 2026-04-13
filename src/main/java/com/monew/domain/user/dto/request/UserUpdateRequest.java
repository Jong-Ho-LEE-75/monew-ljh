package com.monew.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
    @NotBlank
    @Size(min = 1, max = 20)
    String nickname
) {

}
