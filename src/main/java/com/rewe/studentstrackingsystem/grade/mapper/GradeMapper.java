package com.rewe.studentstrackingsystem.grade.mapper;

import com.rewe.studentstrackingsystem.grade.dto.GradeRequest;
import com.rewe.studentstrackingsystem.grade.dto.GradeResponse;
import com.rewe.studentstrackingsystem.grade.entity.Grade;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GradeMapper {

    Grade toEntity(GradeRequest gradeRequest);

    GradeResponse toResponse(Grade grade);
}
