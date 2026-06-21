package com.rewe.studentstrackingsystem.grade.dto;

import java.time.LocalDate;
import java.util.UUID;

public record GradeRequest(
        Double grade,
        LocalDate creationDate,
        UUID student,
        UUID course
) {
}
