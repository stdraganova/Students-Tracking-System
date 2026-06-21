package com.rewe.studentstrackingsystem.teacher.mapper;

import com.rewe.studentstrackingsystem.teacher.dtos.TeacherRequest;
import com.rewe.studentstrackingsystem.teacher.dtos.TeacherResponse;
import com.rewe.studentstrackingsystem.teacher.entity.Teacher;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TeacherMapper {

    Teacher toEntity(TeacherRequest teacherRequest);

    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "email", source = "user.email")
    TeacherResponse toResponse(Teacher teacher);
}
