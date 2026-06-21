package com.rewe.studentstrackingsystem.user.services;

import com.rewe.studentstrackingsystem.user.dto.UserRequest;
import com.rewe.studentstrackingsystem.user.entity.Role;
import com.rewe.studentstrackingsystem.user.entity.User;
import com.rewe.studentstrackingsystem.user.mapper.UserMapper;
import com.rewe.studentstrackingsystem.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper mapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserRequest request;
    private User user;

    @BeforeEach
    void setUp() {
        request = new UserRequest(
                "john",
                "password123",
                Role.TEACHER,
                "John",
                "Doe",
                "john@email.com",
                null
        );

        user = new User();
    }

    @Test
    void shouldCreateTeacherUser() {
        user.setRole(Role.TEACHER);
        user.setPassword("raw");

        when(mapper.toEntity(request)).thenReturn(user);
        when(passwordEncoder.encode("raw")).thenReturn("encoded");
        when(userRepository.save(user)).thenReturn(user);

        userService.save(request);

        assertNotNull(user.getTeacher());
        assertEquals(user, user.getTeacher().getUser());
        assertEquals("encoded", user.getPassword());

        verify(userRepository).save(user);
    }

    @Test
    void shouldCreateStudentUser() {
        user.setRole(Role.STUDENT);
        user.setPassword("raw");

        when(mapper.toEntity(request)).thenReturn(user);
        when(passwordEncoder.encode("raw")).thenReturn("encoded");
        when(userRepository.save(user)).thenReturn(user);

        userService.save(request);

        assertNotNull(user.getStudent());
        assertEquals(user, user.getStudent().getUser());
        assertEquals("encoded", user.getPassword());

        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowExceptionWhenAdmin() {
        user.setRole(Role.ADMIN);
        user.setPassword("raw");

        when(mapper.toEntity(request)).thenReturn(user);

        com.rewe.studentstrackingsystem.exception.InvalidOperationException ex =
                assertThrows(com.rewe.studentstrackingsystem.exception.InvalidOperationException.class,
                        () -> userService.save(request));
        assertEquals("Cannot create ADMIN users via API", ex.getMessage());

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldDeleteUserById() {
        UUID id = UUID.randomUUID();

        when(userRepository.existsById(id)).thenReturn(true);

        userService.delete(id);

        verify(userRepository).deleteById(id);
}