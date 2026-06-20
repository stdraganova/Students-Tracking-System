package com.rewe.studentstrackingsystem.user.dto;

import com.rewe.studentstrackingsystem.user.entity.Role;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UserRequest(
        @NotEmpty String username,
        @NotEmpty String password,
        @NotNull Role role,
        @NotEmpty String firstName,
        @NotEmpty String lastName,
        @NotEmpty String email,
        String profilePictureUrl
) {
}