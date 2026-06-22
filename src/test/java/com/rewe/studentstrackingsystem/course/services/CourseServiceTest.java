package com.rewe.studentstrackingsystem.course.services;

import com.rewe.studentstrackingsystem.course.dto.CourseRequest;
import com.rewe.studentstrackingsystem.course.dto.CourseResponse;
import com.rewe.studentstrackingsystem.course.entity.Course;
import com.rewe.studentstrackingsystem.course.mapper.CourseMapper;
import com.rewe.studentstrackingsystem.course.repository.CourseRepository;
import com.rewe.studentstrackingsystem.exception.ResourceNotFoundException;
import com.rewe.studentstrackingsystem.teacher.entity.Teacher;
import com.rewe.studentstrackingsystem.teacher.repository.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private CourseMapper courseMapper;

    @InjectMocks
    private CourseService courseService;

    private CourseRequest request;
    private Teacher teacher;
    private Course course;
    private CourseResponse response;

    @BeforeEach
    void setUp() {
        UUID teacherId = UUID.randomUUID();
        request = new CourseRequest("Math", teacherId);

        teacher = new Teacher();
        teacher.setId(teacherId);

        course = new Course();
        course.setId(UUID.randomUUID());
        course.setName("Math");

        response = null;
    }

    @Test
    void saveShouldPersistCourse() {
        courseService.save(course);

        verify(courseRepository).save(course);
    }

    @Test
    void createShouldReturnMappedResponse() {
        Course mappedCourse = new Course();
        mappedCourse.setName("Math");
        when(courseMapper.toEntity(request)).thenReturn(mappedCourse);
        when(teacherRepository.findById(request.teacherId())).thenReturn(Optional.of(teacher));
        when(courseRepository.save(mappedCourse)).thenReturn(mappedCourse);
        when(courseMapper.toResponse(mappedCourse)).thenReturn(response);

        CourseResponse result = courseService.create(request);

        assertNull(result);
        assertEquals(teacher, mappedCourse.getTeacher());
        assertTrue(teacher.getCourses().contains(mappedCourse));
        verify(teacherRepository).save(teacher);
        verify(courseRepository).save(mappedCourse);
    }

    @Test
    void createShouldThrowWhenTeacherMissing() {
        when(courseMapper.toEntity(request)).thenReturn(new Course());
        when(teacherRepository.findById(request.teacherId())).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> courseService.create(request));

        assertEquals("Teacher not found with id: " + request.teacherId(), ex.getMessage());
        verify(courseRepository, never()).save(any());
    }

    @Test
    void getCourseByIdShouldReturnCourse() {
        UUID id = UUID.randomUUID();
        when(courseRepository.findById(id)).thenReturn(Optional.of(course));

        Course result = courseService.getCourseById(id);

        assertEquals(course, result);
    }

    @Test
    void deleteShouldRemoveCourseWhenItExists() {
        UUID id = UUID.randomUUID();
        when(courseRepository.existsById(id)).thenReturn(true);

        courseService.delete(id);

        verify(courseRepository).deleteById(id);
    }

    @Test
    void deleteShouldThrowWhenCourseMissing() {
        UUID id = UUID.randomUUID();
        when(courseRepository.existsById(id)).thenReturn(false);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> courseService.delete(id));

        assertEquals("Course not found with id: " + id, ex.getMessage());
        verify(courseRepository, never()).deleteById(any());
    }
}

