package com.rewe.studentstrackingsystem.student.mapper;

import com.rewe.studentstrackingsystem.student.dto.StudentResponse;
import com.rewe.studentstrackingsystem.student.entity.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StudentMapper {

    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "email", source = "user.email")
    StudentResponse toResponse(Student student);
}
