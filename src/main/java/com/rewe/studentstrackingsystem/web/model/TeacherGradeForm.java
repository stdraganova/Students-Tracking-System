package com.rewe.studentstrackingsystem.web.model;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TeacherGradeForm(
        @NotNull(message = "Grade is required")
        @DecimalMin(value = "2.0", message = "Grade must be at least 2")
        @DecimalMax(value = "6.0", message = "Grade must be at most 6")
        Double grade,
        @NotNull(message = "Grade date is required")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate creationDate
) {
}

