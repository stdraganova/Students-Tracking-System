package com.rewe.studentstrackingsystem.teacher.dtos;

import java.util.UUID;

public record TeacherRequest(
        String firstName,
        String lastName,
        String email,
        UUID userId
) {
}