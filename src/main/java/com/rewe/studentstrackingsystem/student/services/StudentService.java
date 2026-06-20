package com.rewe.studentstrackingsystem.student.services;

import com.rewe.studentstrackingsystem.student.dto.StudentRequest;
import com.rewe.studentstrackingsystem.student.dto.StudentResponse;
import com.rewe.studentstrackingsystem.student.mapper.StudentMapper;
import com.rewe.studentstrackingsystem.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper mapper;

    public StudentResponse save (StudentRequest studentRequest) {
        var savedStudent = studentRepository.save(mapper.toEntity(studentRequest));
        return mapper.toResponse(savedStudent);
    }

    public void delete(UUID studentId) {
        studentRepository.deleteById(studentId);
    }
}
