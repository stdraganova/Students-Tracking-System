package com.rewe.studentstrackingsystem.teacher.dtos;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TeacherRequest(
        @NotNull UUID userId
) {
}