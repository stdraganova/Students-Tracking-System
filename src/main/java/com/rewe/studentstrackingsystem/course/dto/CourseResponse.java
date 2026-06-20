package com.rewe.studentstrackingsystem.course.dto;

import com.rewe.studentstrackingsystem.student.dto.StudentResponse;
import com.rewe.studentstrackingsystem.teacher.entity.Teacher;

import java.util.List;

public record CourseResponse(
        String name,
        Teacher teacher,
        List<StudentResponse> students
) {
}
