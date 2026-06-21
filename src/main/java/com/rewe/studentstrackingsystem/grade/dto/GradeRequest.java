package com.rewe.studentstrackingsystem.grade.dto;

import com.rewe.studentstrackingsystem.course.entity.Course;
import com.rewe.studentstrackingsystem.student.entity.Student;

import java.time.LocalDate;
import java.util.UUID;

public record GradeRequest(
        Double grade,
        LocalDate creationDate,
        UUID student,
        UUID course
) {
}
