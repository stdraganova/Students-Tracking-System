package com.rewe.studentstrackingsystem.student.services;

import com.rewe.studentstrackingsystem.attendance.dtos.AttendanceRequest;
import com.rewe.studentstrackingsystem.attendance.services.AttendanceService;
import com.rewe.studentstrackingsystem.course.services.CourseService;
import com.rewe.studentstrackingsystem.exception.ResourceNotFoundException;
import com.rewe.studentstrackingsystem.exception.ValidationException;
import com.rewe.studentstrackingsystem.grade.dto.GradeRequest;
import com.rewe.studentstrackingsystem.grade.services.GradeService;
import com.rewe.studentstrackingsystem.student.dto.StudentRequest;
import com.rewe.studentstrackingsystem.student.dto.StudentResponse;
import com.rewe.studentstrackingsystem.student.entity.Student;
import com.rewe.studentstrackingsystem.student.mapper.StudentMapper;
import com.rewe.studentstrackingsystem.student.repository.StudentRepository;
import com.rewe.studentstrackingsystem.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StudentService {

    private static final String STUDENT_ID_NULL_MSG = "Student ID cannot be null";
    private static final String STUDENT_RESOURCE = "Student";

    private final StudentRepository studentRepository;
    private final StudentMapper mapper;
    private final AttendanceService attendanceService;
    private final CourseService courseService;
    private final GradeService gradeService;
    private final UserRepository userRepository;

    public StudentResponse save(StudentRequest studentRequest) {
        Objects.requireNonNull(studentRequest, "StudentRequest cannot be null");

        var user = userRepository.findById(studentRequest.userId())
                .orElseThrow(() -> ResourceNotFoundException.of("User", studentRequest.userId().toString()));

        var student = new Student();
        student.setUser(user);
        user.setStudent(student);

        var savedStudent = studentRepository.save(student);
        log.info("Student saved successfully: {}", savedStudent.getId());
        return mapper.toResponse(savedStudent);
    }

    public void addAttendance(AttendanceRequest attendanceRequest) {
        Objects.requireNonNull(attendanceRequest, "AttendanceRequest cannot be null");

        var student = studentRepository.findById(attendanceRequest.studentId())
                .orElseThrow(() -> ResourceNotFoundException.of(STUDENT_RESOURCE, attendanceRequest.studentId().toString()));

        var attendance = attendanceService.create(attendanceRequest, student);

        student.getAttendances().add(attendance);
        studentRepository.save(student);
        log.info("Attendance record added for student: {}", student.getId());
    }

    public void removeAttendance(UUID attendanceId, UUID studentId) {
        Objects.requireNonNull(attendanceId, "Attendance ID cannot be null");
        Objects.requireNonNull(studentId, STUDENT_ID_NULL_MSG);

        var student = studentRepository.findById(studentId)
                .orElseThrow(() -> ResourceNotFoundException.of(STUDENT_RESOURCE, studentId.toString()));

        boolean removed = student.getAttendances()
                .removeIf(attendance -> attendance.getId().equals(attendanceId));

        if (!removed) {
            log.warn("Attendance record {} not found for student {}", attendanceId, studentId);
            throw new ValidationException("Attendance not found for this student");
        }

        attendanceService.delete(attendanceId);
        studentRepository.save(student);
        log.info("Attendance record removed from student: {}", studentId);
    }

    public void addCourse(UUID courseId, UUID studentId) {
        Objects.requireNonNull(courseId, "Course ID cannot be null");
        Objects.requireNonNull(studentId, STUDENT_ID_NULL_MSG);

        var course = courseService.getCourseById(courseId);
        var student = studentRepository.findById(studentId)
                .orElseThrow(() -> ResourceNotFoundException.of(STUDENT_RESOURCE, studentId.toString()));

        var studentAlreadyEnrolled = student.getCourses().stream().anyMatch(c -> c.getId().equals(courseId));

        if (studentAlreadyEnrolled) {
            log.warn("Student {} already enrolled in course {}", studentId, courseId);
            throw new ValidationException("Student is already enrolled in this course");
        }

        student.getCourses().add(course);
        course.getStudents().add(student);

        studentRepository.save(student);
        courseService.save(course);
        log.info("Course {} added to student {}", courseId, studentId);
    }

    public void addCourseForUsername(UUID courseId, String username) {
        Objects.requireNonNull(courseId, "Course ID cannot be null");
        Objects.requireNonNull(username, "Username cannot be null");

        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> ResourceNotFoundException.of("User", username));

        if (user.getStudent() == null) {
            throw new ValidationException("Student profile not found");
        }

        addCourse(courseId, user.getStudent().getId());
    }

    public void removeCourse(UUID courseId, UUID studentId) {
        Objects.requireNonNull(courseId, "Course ID cannot be null");
        Objects.requireNonNull(studentId, STUDENT_ID_NULL_MSG);

        var course = courseService.getCourseById(courseId);
        var student = studentRepository.findById(studentId)
                .orElseThrow(() -> ResourceNotFoundException.of(STUDENT_RESOURCE, studentId.toString()));

        boolean studentRemoved = student.getCourses().remove(course);
        boolean courseRemoved = course.getStudents().remove(student);

        if (!studentRemoved || !courseRemoved) {
            log.warn("Failed to remove course {} from student {}", courseId, studentId);
            throw new ValidationException("Course is not assigned to this student");
        }

        studentRepository.save(student);
        courseService.save(course);
        log.info("Course {} removed from student {}", courseId, studentId);
    }

    public void addGrade(GradeRequest gradeRequest) {
        Objects.requireNonNull(gradeRequest, "GradeRequest cannot be null");

        var student = studentRepository.findById(gradeRequest.student())
                .orElseThrow(() -> ResourceNotFoundException.of(STUDENT_RESOURCE, gradeRequest.student().toString()));

        var grade = gradeService.create(gradeRequest, student);

        student.getGrades().add(grade);
        studentRepository.save(student);
        log.info("Grade added for student: {}", student.getId());
    }

    public void removeGrade(UUID studentId, UUID gradeId) {
        Objects.requireNonNull(studentId, STUDENT_ID_NULL_MSG);
        Objects.requireNonNull(gradeId, "Grade ID cannot be null");

        var student = studentRepository.findById(studentId)
                .orElseThrow(() -> ResourceNotFoundException.of(STUDENT_RESOURCE, studentId.toString()));

        boolean removed = student.getGrades().removeIf(grade -> grade.getId().equals(gradeId));

        if (!removed) {
            log.warn("Grade {} not found for student {}", gradeId, studentId);
            throw new ValidationException("Grade not found for this student");
        }

        gradeService.delete(gradeId);
        studentRepository.save(student);
        log.info("Grade removed from student: {}", studentId);
    }

    public void delete(UUID studentId) {
        Objects.requireNonNull(studentId, STUDENT_ID_NULL_MSG);

        if (!studentRepository.existsById(studentId)) {
            throw ResourceNotFoundException.of(STUDENT_RESOURCE, studentId.toString());
        }

        log.info("Deleting student: {}", studentId);
        studentRepository.deleteById(studentId);
    }
}
