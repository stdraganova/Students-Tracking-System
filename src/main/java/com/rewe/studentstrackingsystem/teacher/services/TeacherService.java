package com.rewe.studentstrackingsystem.teacher.services;

import com.rewe.studentstrackingsystem.exception.ResourceNotFoundException;
import com.rewe.studentstrackingsystem.teacher.dtos.TeacherRequest;
import com.rewe.studentstrackingsystem.teacher.dtos.TeacherResponse;
import com.rewe.studentstrackingsystem.teacher.entity.Teacher;
import com.rewe.studentstrackingsystem.teacher.mapper.TeacherMapper;
import com.rewe.studentstrackingsystem.teacher.repository.TeacherRepository;
import com.rewe.studentstrackingsystem.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final TeacherMapper mapper;
    private final UserRepository userRepository;

    public TeacherResponse save(TeacherRequest teacherRequest) {
        Objects.requireNonNull(teacherRequest, "TeacherRequest cannot be null");

        var user = userRepository.findById(teacherRequest.userId())
                .orElseThrow(() -> ResourceNotFoundException.of("User", teacherRequest.userId().toString()));

        var teacher = new Teacher();
        teacher.setUser(user);
        user.setTeacher(teacher);

        var savedTeacher = teacherRepository.save(teacher);
        log.info("Teacher saved successfully: {}", savedTeacher.getId());
        return mapper.toResponse(savedTeacher);
    }

    @Transactional(readOnly = true)
    public List<Teacher> getAll() {
        return teacherRepository.findAll();
    }

    public void delete(UUID teacherId) {
        Objects.requireNonNull(teacherId, "Teacher ID cannot be null");

        if (!teacherRepository.existsById(teacherId)) {
            throw ResourceNotFoundException.of("Teacher", teacherId.toString());
        }

        log.info("Deleting teacher: {}", teacherId);
        teacherRepository.deleteById(teacherId);
    }
}
