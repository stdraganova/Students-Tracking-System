package com.rewe.studentstrackingsystem.attendance.dtos;

import java.time.LocalDate;
import java.util.UUID;

public record AttendanceResponse(
        UUID id,
        LocalDate attendanceDate,
        boolean present,
        UUID studentId,
        UUID teacherId,
        UUID courseId
) {
}

