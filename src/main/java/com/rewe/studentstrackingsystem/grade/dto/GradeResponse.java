package com.rewe.studentstrackingsystem.grade.dto;

import java.time.LocalDate;
import java.util.UUID;

public record GradeResponse(
        Double grade,
        LocalDate creationDate,
        UUID studentId,
        UUID courseId
) {
}
