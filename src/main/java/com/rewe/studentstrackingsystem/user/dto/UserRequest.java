package com.rewe.studentstrackingsystem.user.dto;

import com.rewe.studentstrackingsystem.user.entity.Role;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotEmpty
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,
        @NotEmpty
        @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
        String password,
        @NotNull Role role,
        @NotEmpty String firstName,
        @NotEmpty String lastName,
        @NotEmpty String email,
        String profilePictureUrl
) {
}