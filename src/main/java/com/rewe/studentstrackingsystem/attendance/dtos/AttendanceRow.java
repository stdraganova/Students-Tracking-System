package com.rewe.studentstrackingsystem.attendance.dtos;

public record AttendanceRow(String id, java.time.LocalDate date, boolean present) {
}
