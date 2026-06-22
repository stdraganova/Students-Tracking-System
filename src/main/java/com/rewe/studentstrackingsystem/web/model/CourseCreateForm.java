package com.rewe.studentstrackingsystem.web.model;

import jakarta.validation.constraints.NotBlank;

public record CourseCreateForm(
        @NotBlank(message = "Course name is required")
        String name,
        @NotBlank(message = "Teacher is required")
        String teacherId
) {
}

