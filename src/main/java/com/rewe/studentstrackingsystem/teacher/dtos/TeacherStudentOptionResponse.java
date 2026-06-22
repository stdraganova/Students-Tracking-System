package com.rewe.studentstrackingsystem.teacher.dtos;

import java.util.UUID;

public record TeacherStudentOptionResponse(
        UUID id,
        String firstName,
        String lastName,
        String email
) {
}

