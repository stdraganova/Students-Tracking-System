package com.rewe.studentstrackingsystem.web.model;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TeacherAttendanceForm(
        @NotNull(message = "Attendance date is required")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate attendanceDate,
        @NotNull(message = "Presence flag is required")
        Boolean present
) {
}

