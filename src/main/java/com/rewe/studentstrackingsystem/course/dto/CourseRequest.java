package com.rewe.studentstrackingsystem.course.dto;

import com.rewe.studentstrackingsystem.teacher.entity.Teacher;

public record CourseRequest(
        String name,
        Teacher teacher
) {
}
