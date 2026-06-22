package com.rewe.studentstrackingsystem.grade.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record GradeRequest(
        @NotNull
        @Min(value = 2, message = "Grade must be at least 2")
        @Max(value = 6, message = "Grade must be at most 6")
        Double grade,
        @NotNull
        LocalDate creationDate,
        @NotNull
        UUID student,
        @NotNull
        UUID course
) {
}
