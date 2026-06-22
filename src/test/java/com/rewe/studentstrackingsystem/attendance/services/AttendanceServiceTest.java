package com.rewe.studentstrackingsystem.attendance.services;

import com.rewe.studentstrackingsystem.attendance.dtos.AttendanceRequest;
import com.rewe.studentstrackingsystem.attendance.entity.Attendance;
import com.rewe.studentstrackingsystem.attendance.repository.AttendanceRepository;
import com.rewe.studentstrackingsystem.course.entity.Course;
import com.rewe.studentstrackingsystem.course.repository.CourseRepository;
import com.rewe.studentstrackingsystem.exception.ResourceNotFoundException;
import com.rewe.studentstrackingsystem.exception.ValidationException;
import com.rewe.studentstrackingsystem.student.entity.Student;
import com.rewe.studentstrackingsystem.teacher.entity.Teacher;
import com.rewe.studentstrackingsystem.teacher.repository.TeacherRepository;
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
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private AttendanceRequest request;
    private Student student;
    private Teacher teacher;
    private Course course;

    @BeforeEach
    void setUp() {
        UUID studentId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        request = new AttendanceRequest(LocalDate.of(2026, 1, 15), true, studentId, teacherId, courseId);

        student = new Student();
        student.setId(studentId);

        teacher = new Teacher();
        teacher.setId(teacherId);

        course = new Course();
        course.setId(courseId);
    }

    @Test
    void createShouldSaveAttendance() {
        when(teacherRepository.findById(request.teacherId())).thenReturn(Optional.of(teacher));
        when(courseRepository.findById(request.courseId())).thenReturn(Optional.of(course));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Attendance result = attendanceService.create(request, student);

        assertNotNull(result);
        assertEquals(request.attendanceDate(), result.getAttendanceDate());
        assertEquals(request.isPresent(), result.isPresent());
        assertEquals(student, result.getStudent());
        assertEquals(teacher, result.getTeacher());
        assertEquals(course, result.getCourse());
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    void createShouldThrowWhenAttendanceDateIsNull() {
        AttendanceRequest invalid = new AttendanceRequest(null, true, request.studentId(), request.teacherId(), request.courseId());

        ValidationException ex = assertThrows(ValidationException.class,
                () -> attendanceService.create(invalid, student));

        assertEquals("Attendance date cannot be null", ex.getMessage());
        verifyNoInteractions(teacherRepository, courseRepository, attendanceRepository);
    }

    @Test
    void createShouldThrowWhenTeacherMissing() {
        when(teacherRepository.findById(request.teacherId())).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> attendanceService.create(request, student));

        assertEquals("Teacher not found with id: " + request.teacherId(), ex.getMessage());
        verify(courseRepository, never()).findById(any());
        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void deleteShouldRemoveAttendanceWhenItExists() {
        UUID attendanceId = UUID.randomUUID();
        when(attendanceRepository.existsById(attendanceId)).thenReturn(true);

        attendanceService.delete(attendanceId);

        verify(attendanceRepository).deleteById(attendanceId);
    }

    @Test
    void deleteShouldThrowWhenAttendanceMissing() {
        UUID attendanceId = UUID.randomUUID();
        when(attendanceRepository.existsById(attendanceId)).thenReturn(false);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> attendanceService.delete(attendanceId));

        assertEquals("Attendance not found with id: " + attendanceId, ex.getMessage());
        verify(attendanceRepository, never()).deleteById(any());
    }
}

