package com.rewe.studentstrackingsystem.student.dto;

import com.rewe.studentstrackingsystem.user.entity.User;

public record StudentRequest(
        String firstName,
        String lastName,
        String email,
        User user
) {
}
