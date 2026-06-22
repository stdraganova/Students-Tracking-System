package com.rewe.studentstrackingsystem.student.services;

import com.rewe.studentstrackingsystem.attendance.dtos.AttendanceRequest;
import com.rewe.studentstrackingsystem.attendance.entity.Attendance;
import com.rewe.studentstrackingsystem.attendance.services.AttendanceService;
import com.rewe.studentstrackingsystem.course.entity.Course;
import com.rewe.studentstrackingsystem.course.services.CourseService;
import com.rewe.studentstrackingsystem.exception.ResourceNotFoundException;
import com.rewe.studentstrackingsystem.exception.ValidationException;
import com.rewe.studentstrackingsystem.grade.dto.GradeRequest;
import com.rewe.studentstrackingsystem.grade.entity.Grade;
import com.rewe.studentstrackingsystem.grade.services.GradeService;
import com.rewe.studentstrackingsystem.student.dto.StudentRequest;
import com.rewe.studentstrackingsystem.student.dto.StudentResponse;
import com.rewe.studentstrackingsystem.student.entity.Student;
import com.rewe.studentstrackingsystem.student.mapper.StudentMapper;
import com.rewe.studentstrackingsystem.student.repository.StudentRepository;
import com.rewe.studentstrackingsystem.user.entity.User;
import com.rewe.studentstrackingsystem.user.repository.UserRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

	@Mock
	private StudentRepository studentRepository;

	@Mock
	private StudentMapper mapper;

	@Mock
	private AttendanceService attendanceService;

	@Mock
	private CourseService courseService;

	@Mock
	private GradeService gradeService;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private StudentService studentService;

	private StudentRequest studentRequest;
	private Student student;

	@BeforeEach
	void setUp() {
		studentRequest = new StudentRequest(UUID.randomUUID());
		student = new Student();
		student.setId(UUID.randomUUID());
	}

	@Test
	void saveShouldPersistStudent() {
		User user = new User();
		user.setId(studentRequest.userId());
		final Student[] savedStudent = new Student[1];

		when(userRepository.findById(studentRequest.userId())).thenReturn(Optional.of(user));
		when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> {
			savedStudent[0] = invocation.getArgument(0);
			return savedStudent[0];
		});
		when(mapper.toResponse(any(Student.class))).thenReturn(null);

		StudentResponse result = studentService.save(studentRequest);

		assertNull(result);
		assertNotNull(savedStudent[0]);
		assertEquals(user, savedStudent[0].getUser());
		assertEquals(savedStudent[0], user.getStudent());
		verify(userRepository).findById(studentRequest.userId());
		verify(studentRepository).save(any(Student.class));
		verify(mapper).toResponse(savedStudent[0]);
	}

	@Test
	void addAttendanceShouldSaveAttendanceForStudent() {
		UUID studentId = UUID.randomUUID();
		UUID teacherId = UUID.randomUUID();
		UUID courseId = UUID.randomUUID();
		student.setId(studentId);

		AttendanceRequest request = new AttendanceRequest(LocalDate.of(2026, 3, 1), true, studentId, teacherId, courseId);
		Attendance attendance = new Attendance();

		when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
		when(attendanceService.create(request, student)).thenReturn(attendance);
		when(studentRepository.save(student)).thenReturn(student);

		studentService.addAttendance(request);

		assertTrue(student.getAttendances().contains(attendance));
		verify(attendanceService).create(request, student);
		verify(studentRepository).save(student);
	}

	@Test
	void removeAttendanceShouldDeleteAttendance() {
		UUID studentId = UUID.randomUUID();
		UUID attendanceId = UUID.randomUUID();
		student.setId(studentId);
		Attendance attendance = new Attendance();
		attendance.setId(attendanceId);
		student.getAttendances().add(attendance);

		when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
		when(studentRepository.save(student)).thenReturn(student);

		studentService.removeAttendance(attendanceId, studentId);

		assertTrue(student.getAttendances().isEmpty());
		verify(attendanceService).delete(attendanceId);
		verify(studentRepository).save(student);
	}

	@Test
	void addCourseShouldAddCourseToStudent() {
		UUID studentId = UUID.randomUUID();
		UUID courseId = UUID.randomUUID();
		student.setId(studentId);

		Course course = new Course();
		course.setId(courseId);

		when(courseService.getCourseById(courseId)).thenReturn(course);
		when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
		when(studentRepository.save(student)).thenReturn(student);

		studentService.addCourse(courseId, studentId);

		assertTrue(student.getCourses().contains(course));
		assertTrue(course.getStudents().contains(student));
		verify(studentRepository).save(student);
		verify(courseService).save(course);
	}

	@Test
	void addCourseShouldThrowOnDuplicateEnrollment() {
		UUID studentId = UUID.randomUUID();
		UUID courseId = UUID.randomUUID();
		student.setId(studentId);

		Course course = new Course();
		course.setId(courseId);
		student.getCourses().add(course);

		when(courseService.getCourseById(courseId)).thenReturn(course);
		when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

		ValidationException ex = assertThrows(ValidationException.class,
				() -> studentService.addCourse(courseId, studentId));

		assertEquals("Student is already enrolled in this course", ex.getMessage());
		verify(studentRepository, never()).save(any());
		verify(courseService, never()).save(any());
	}

	@Test
	void removeCourseShouldRemoveCourseFromStudent() {
		UUID studentId = UUID.randomUUID();
		UUID courseId = UUID.randomUUID();
		student.setId(studentId);

		Course course = new Course();
		course.setId(courseId);
		student.getCourses().add(course);
		course.getStudents().add(student);

		when(courseService.getCourseById(courseId)).thenReturn(course);
		when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
		when(studentRepository.save(student)).thenReturn(student);

		studentService.removeCourse(courseId, studentId);

		assertFalse(student.getCourses().contains(course));
		assertFalse(course.getStudents().contains(student));
		verify(studentRepository).save(student);
		verify(courseService).save(course);
	}

	@Test
	void addGradeShouldSaveGradeForStudent() {
		UUID studentId = UUID.randomUUID();
		UUID courseId = UUID.randomUUID();
		student.setId(studentId);

		GradeRequest request = new GradeRequest(5.0, LocalDate.of(2026, 4, 1), studentId, courseId);
		Grade grade = Grade.builder().build();

		when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
		when(gradeService.create(request, student)).thenReturn(grade);
		when(studentRepository.save(student)).thenReturn(student);

		studentService.addGrade(request);

		assertTrue(student.getGrades().contains(grade));
		verify(gradeService).create(request, student);
		verify(studentRepository).save(student);
	}

	@Test
	void removeGradeShouldDeleteGrade() {
		UUID studentId = UUID.randomUUID();
		UUID gradeId = UUID.randomUUID();
		student.setId(studentId);
		Grade grade = Grade.builder().id(gradeId).build();
		student.getGrades().add(grade);

		when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
		when(studentRepository.save(student)).thenReturn(student);

		studentService.removeGrade(studentId, gradeId);

		assertTrue(student.getGrades().isEmpty());
		verify(gradeService).delete(gradeId);
		verify(studentRepository).save(student);
	}

	@Test
	void deleteShouldRemoveStudentWhenItExists() {
		UUID studentId = UUID.randomUUID();
		when(studentRepository.existsById(studentId)).thenReturn(true);

		studentService.delete(studentId);

		verify(studentRepository).deleteById(studentId);
	}

	@Test
	void deleteShouldThrowWhenStudentMissing() {
		UUID studentId = UUID.randomUUID();
		when(studentRepository.existsById(studentId)).thenReturn(false);

		ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
				() -> studentService.delete(studentId));

		assertEquals("Student not found with id: " + studentId, ex.getMessage());
		verify(studentRepository, never()).deleteById(any());
	}
}

