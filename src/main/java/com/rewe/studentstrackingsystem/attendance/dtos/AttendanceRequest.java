package com.rewe.studentstrackingsystem.attendance.dtos;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record AttendanceRequest(
    @NotNull LocalDate attendanceDate,
    boolean isPresent,
    @NotNull UUID studentId,
    @NotNull UUID teacherId,
    @NotNull UUID courseId
) {
}
