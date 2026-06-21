package com.rewe.studentstrackingsystem.course.mapper;

import com.rewe.studentstrackingsystem.course.dto.CourseRequest;
import com.rewe.studentstrackingsystem.course.dto.CourseResponse;
import com.rewe.studentstrackingsystem.course.entity.Course;
import com.rewe.studentstrackingsystem.student.mapper.StudentMapper;
import com.rewe.studentstrackingsystem.teacher.mapper.TeacherMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {TeacherMapper.class, StudentMapper.class})
public interface CourseMapper {

    Course toEntity(CourseRequest courseRequest);

    CourseResponse toResponse(Course course);
}
