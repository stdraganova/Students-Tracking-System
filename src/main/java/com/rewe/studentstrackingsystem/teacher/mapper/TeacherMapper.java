package com.rewe.studentstrackingsystem.teacher.mapper;

import com.rewe.studentstrackingsystem.teacher.dtos.TeacherRequest;
import com.rewe.studentstrackingsystem.teacher.dtos.TeacherResponse;
import com.rewe.studentstrackingsystem.teacher.entity.Teacher;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TeacherMapper {

    Teacher toEntity(TeacherRequest teacherRequest);

    TeacherResponse toResponse(Teacher teacher);
}
