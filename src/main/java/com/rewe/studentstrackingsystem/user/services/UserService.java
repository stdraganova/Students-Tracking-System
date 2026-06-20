package com.rewe.studentstrackingsystem.user.services;

import com.rewe.studentstrackingsystem.student.entity.Student;
import com.rewe.studentstrackingsystem.teacher.entity.Teacher;
import com.rewe.studentstrackingsystem.user.dto.UserRequest;
import com.rewe.studentstrackingsystem.user.dto.UserResponse;
import com.rewe.studentstrackingsystem.user.mapper.UserMapper;
import com.rewe.studentstrackingsystem.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    public UserResponse save(UserRequest userRequest) {
        var user = mapper.toEntity(userRequest);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        switch (user.getRole()) {
            case TEACHER -> {
                var teacher = new Teacher();
                teacher.setUser(user);
                user.setTeacher(teacher);
            }
            case STUDENT -> {
                var student = new Student();
                student.setUser(user);
                user.setStudent(student);
            }
            case ADMIN -> throw new IllegalArgumentException("Cannot create ADMIN users via API");
        }

        var savedUser = userRepository.save(user);
        return mapper.toResponse(savedUser);
    }

    public void delete(UUID id) {
        userRepository.deleteById(id);
    }
}
