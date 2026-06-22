package com.rewe.studentstrackingsystem.course.dto;

import java.util.UUID;

public record CourseRequest(
        String name,
        UUID teacherId
) {
}
