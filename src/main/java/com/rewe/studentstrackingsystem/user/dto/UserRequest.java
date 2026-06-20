package com.rewe.studentstrackingsystem.user.dto;

import com.rewe.studentstrackingsystem.user.entity.Role;

public record UserRequest(
        String username,
        String password,
        Role role,
        String firstName,
        String lastName,
        String email,
        String profilePictureUrl
) {
}