package com.rewe.studentstrackingsystem.grade.services;

import com.rewe.studentstrackingsystem.course.entity.Course;
import com.rewe.studentstrackingsystem.course.repository.CourseRepository;
import com.rewe.studentstrackingsystem.exception.ResourceNotFoundException;
import com.rewe.studentstrackingsystem.exception.ValidationException;
import com.rewe.studentstrackingsystem.grade.dto.GradeRequest;
import com.rewe.studentstrackingsystem.grade.entity.Grade;
import com.rewe.studentstrackingsystem.grade.mapper.GradeMapper;
import com.rewe.studentstrackingsystem.grade.repository.GradeRepository;
import com.rewe.studentstrackingsystem.student.entity.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private GradeMapper gradeMapper;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private GradeService gradeService;

    private GradeRequest request;
    private Student student;
    private Course course;
    private Grade grade;

    @BeforeEach
    void setUp() {
        UUID studentId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        request = new GradeRequest(5.0, LocalDate.of(2026, 2, 1), studentId, courseId);
        student = new Student();
        student.setId(studentId);

        course = new Course();
        course.setId(courseId);

        grade = Grade.builder()
                .id(UUID.randomUUID())
                .grade(5.0)
                .creationDate(LocalDate.of(2026, 2, 1))
                .student(student)
                .course(course)
                .build();
    }

    @Test
    void saveShouldReturnMappedResponse() {
        when(gradeRepository.save(grade)).thenReturn(grade);
        when(gradeMapper.toResponse(grade)).thenReturn(null);

        assertNull(gradeService.save(grade));
        verify(gradeRepository).save(grade);
        verify(gradeMapper).toResponse(grade);
    }

    @Test
    void createShouldSaveGrade() {
        when(courseRepository.findById(request.course())).thenReturn(Optional.of(course));
        when(gradeRepository.save(any(Grade.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Grade result = gradeService.create(request, student);

        assertNotNull(result);
        assertEquals(request.grade(), result.getGrade());
        assertEquals(request.creationDate(), result.getCreationDate());
        assertEquals(student, result.getStudent());
        assertEquals(course, result.getCourse());
        verify(gradeRepository).save(any(Grade.class));
    }

    @Test
    void createShouldThrowWhenGradeOutOfRange() {
        GradeRequest invalid = new GradeRequest(101.0, request.creationDate(), request.student(), request.course());

        ValidationException ex = assertThrows(ValidationException.class,
                () -> gradeService.create(invalid, student));

        assertEquals("Grade must be between 0 and 100", ex.getMessage());
        verifyNoInteractions(courseRepository, gradeRepository);
    }

    @Test
    void createShouldThrowWhenCourseMissing() {
        when(courseRepository.findById(request.course())).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> gradeService.create(request, student));

        assertEquals("Course not found with id: " + request.course(), ex.getMessage());
        verify(gradeRepository, never()).save(any());
    }

    @Test
    void deleteShouldRemoveGradeWhenItExists() {
        UUID gradeId = UUID.randomUUID();
        when(gradeRepository.existsById(gradeId)).thenReturn(true);

        gradeService.delete(gradeId);

        verify(gradeRepository).deleteById(gradeId);
    }

    @Test
    void deleteShouldThrowWhenGradeMissing() {
        UUID gradeId = UUID.randomUUID();
        when(gradeRepository.existsById(gradeId)).thenReturn(false);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> gradeService.delete(gradeId));

        assertEquals("Grade not found with id: " + gradeId, ex.getMessage());
        verify(gradeRepository, never()).deleteById(any());
    }
}

