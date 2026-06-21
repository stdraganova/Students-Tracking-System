package com.rewe.studentstrackingsystem.grade.mapper;

import com.rewe.studentstrackingsystem.grade.dto.GradeRequest;
import com.rewe.studentstrackingsystem.grade.dto.GradeResponse;
import com.rewe.studentstrackingsystem.grade.entity.Grade;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GradeMapper {

    @Mapping(target = "student", ignore = true)
    @Mapping(target = "course", ignore = true)
    Grade toEntity(GradeRequest gradeRequest);

    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "courseId", source = "course.id")
    GradeResponse toResponse(Grade grade);
}
