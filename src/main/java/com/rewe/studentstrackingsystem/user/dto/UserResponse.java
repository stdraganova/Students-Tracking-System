package com.rewe.studentstrackingsystem.user.dto;

public record UserResponse(
        String username,
        String firstName,
        String lastName,
        String email,
        String profilePictureUrl
) {
}
