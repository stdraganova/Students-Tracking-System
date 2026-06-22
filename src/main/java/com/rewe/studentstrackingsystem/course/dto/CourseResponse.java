package com.rewe.studentstrackingsystem.course.dto;

import com.rewe.studentstrackingsystem.student.dto.StudentResponse;
import com.rewe.studentstrackingsystem.teacher.dtos.TeacherResponse;

import java.util.List;

public record CourseResponse(
        String name,
        TeacherResponse teacher,
        List<StudentResponse> students
) {
}
