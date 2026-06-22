package com.rewe.studentstrackingsystem.student.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StudentRequest(
        @NotNull UUID userId
) {
}
