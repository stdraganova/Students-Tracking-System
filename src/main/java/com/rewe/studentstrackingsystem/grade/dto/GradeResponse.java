package com.rewe.studentstrackingsystem.grade.dto;

import com.rewe.studentstrackingsystem.course.entity.Course;
import com.rewe.studentstrackingsystem.student.entity.Student;

import java.time.LocalDate;

public record GradeResponse(
        Double grade,
        LocalDate creationDate,
        Student student,
        Course course
) {
}
