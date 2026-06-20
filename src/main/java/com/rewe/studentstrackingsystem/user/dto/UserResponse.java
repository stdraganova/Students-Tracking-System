package com.rewe.studentstrackingsystem.user.dto;

import com.rewe.studentstrackingsystem.user.entity.Role;

public record UserResponse(
        String username,
        String firstName,
        String lastName,
        String email,
        String profilePictureUrl
) {
}
