package com.rewe.studentstrackingsystem.user.services;

import com.rewe.studentstrackingsystem.exception.InvalidOperationException;
import com.rewe.studentstrackingsystem.exception.ResourceNotFoundException;
import com.rewe.studentstrackingsystem.student.entity.Student;
import com.rewe.studentstrackingsystem.teacher.entity.Teacher;
import com.rewe.studentstrackingsystem.user.dto.UserRequest;
import com.rewe.studentstrackingsystem.user.dto.UserResponse;
import com.rewe.studentstrackingsystem.user.dto.UserUpdateRequest;
import com.rewe.studentstrackingsystem.user.entity.Role;
import com.rewe.studentstrackingsystem.user.entity.User;
import com.rewe.studentstrackingsystem.user.mapper.UserMapper;
import com.rewe.studentstrackingsystem.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    public UserResponse save(UserRequest userRequest) {
        Objects.requireNonNull(userRequest, "UserRequest cannot be null");

        var user = mapper.toEntity(userRequest);
        Objects.requireNonNull(user, "User mapping failed");

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        switch (user.getRole()) {
            case TEACHER -> {
                var teacher = new Teacher();
                teacher.setUser(user);
                user.setTeacher(teacher);
                log.info("Creating TEACHER user: {}", user.getId());
            }
            case STUDENT -> {
                var student = new Student();
                student.setUser(user);
                user.setStudent(student);
                log.info("Creating STUDENT user: {}", user.getId());
            }
            case ADMIN -> {
                log.warn("Attempted to create ADMIN user via API - not allowed");
                throw new InvalidOperationException("Cannot create ADMIN users via API");
            }
        }

        var savedUser = userRepository.save(user);
        log.info("User saved successfully: {}", savedUser.getId());
        return mapper.toResponse(savedUser);
    }

    public UserResponse saveByAdmin(UserRequest userRequest) {
        Objects.requireNonNull(userRequest, "UserRequest cannot be null");

        var user = mapper.toEntity(userRequest);
        Objects.requireNonNull(user, "User mapping failed");

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (user.getRole() == Role.TEACHER) {
            var teacher = new Teacher();
            teacher.setUser(user);
            user.setTeacher(teacher);
        } else if (user.getRole() == Role.STUDENT) {
            var student = new Student();
            student.setUser(user);
            user.setStudent(student);
        }

        var savedUser = userRepository.save(user);
        log.info("User saved by admin successfully: {}", savedUser.getId());
        return mapper.toResponse(savedUser);
    }

    public void delete(UUID id) {
        Objects.requireNonNull(id, "User ID cannot be null");

        if (!userRepository.existsById(id)) {
            throw ResourceNotFoundException.of("User", id.toString());
        }

        log.info("Deleting user: {}", id);
        userRepository.deleteById(id);
    }

    public UserResponse updateSelf(String usernameOrEmail, UserUpdateRequest request) {
        Objects.requireNonNull(usernameOrEmail, "Username or email cannot be null");
        Objects.requireNonNull(request, "UserUpdateRequest cannot be null");

        var user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> ResourceNotFoundException.of("User", usernameOrEmail));

        applyEditableFields(user, request, false);
        return mapper.toResponse(userRepository.save(user));
    }

    private void applyEditableFields(User user, UserUpdateRequest request, boolean allowRoleUpdate) {
        if (hasText(request.username())) {
            user.setUsername(request.username().trim());
        }

        if (hasText(request.password())) {
            user.setPassword(passwordEncoder.encode(request.password().trim()));
        }

        if (hasText(request.firstName())) {
            user.setFirstName(request.firstName().trim());
        }

        if (hasText(request.lastName())) {
            user.setLastName(request.lastName().trim());
        }

        if (hasText(request.email())) {
            user.setEmail(request.email().trim());
        }

        if (request.profilePictureUrl() != null) {
            user.setProfilePictureUrl(request.profilePictureUrl().trim());
        }

        if (allowRoleUpdate && request.role() != null && request.role() != user.getRole()) {
            updateRoleRelations(user, request.role());
        }
    }

    private void updateRoleRelations(User user, Role newRole) {
        switch (newRole) {
            case STUDENT -> {
                user.setTeacher(null);
                if (user.getStudent() == null) {
                    var student = new Student();
                    student.setUser(user);
                    user.setStudent(student);
                }
            }
            case TEACHER -> {
                user.setStudent(null);
                if (user.getTeacher() == null) {
                    var teacher = new Teacher();
                    teacher.setUser(user);
                    user.setTeacher(teacher);
                }
            }
            case ADMIN -> {
                user.setStudent(null);
                user.setTeacher(null);
            }
        }
        user.setRole(newRole);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}