package com.rewe.studentstrackingsystem.course.mapper;

import com.rewe.studentstrackingsystem.course.dto.CourseRequest;
import com.rewe.studentstrackingsystem.course.dto.CourseResponse;
import com.rewe.studentstrackingsystem.course.entity.Course;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    Course toEntity(CourseRequest courseRequest);

    Course toEntity(CourseResponse courseResponse);

    CourseResponse toResponse(Course course);
}
