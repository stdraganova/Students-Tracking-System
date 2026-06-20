package com.rewe.studentstrackingsystem.teacher.services;

import com.rewe.studentstrackingsystem.teacher.dtos.TeacherRequest;
import com.rewe.studentstrackingsystem.teacher.dtos.TeacherResponse;
import com.rewe.studentstrackingsystem.teacher.mapper.TeacherMapper;
import com.rewe.studentstrackingsystem.teacher.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final TeacherMapper mapper;

    public TeacherResponse save(TeacherRequest teacherRequest) {
        var savedTeacher = teacherRepository.save(mapper.toEntity(teacherRequest));
        return mapper.toResponse(savedTeacher);
    }

    public void delete (UUID teacherId) {
        teacherRepository.deleteById(teacherId);
    }
}
