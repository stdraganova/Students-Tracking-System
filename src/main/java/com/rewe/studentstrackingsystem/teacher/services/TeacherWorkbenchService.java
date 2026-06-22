package com.rewe.studentstrackingsystem.teacher.services;

import com.rewe.studentstrackingsystem.attendance.dtos.AttendanceRequest;
import com.rewe.studentstrackingsystem.attendance.dtos.AttendanceResponse;
import com.rewe.studentstrackingsystem.attendance.services.AttendanceService;
import com.rewe.studentstrackingsystem.course.services.CourseService;
import com.rewe.studentstrackingsystem.exception.ResourceNotFoundException;
import com.rewe.studentstrackingsystem.exception.ValidationException;
import com.rewe.studentstrackingsystem.grade.dto.GradeRequest;
import com.rewe.studentstrackingsystem.grade.dto.GradeResponse;
import com.rewe.studentstrackingsystem.grade.services.GradeService;
import com.rewe.studentstrackingsystem.student.repository.StudentRepository;
import com.rewe.studentstrackingsystem.teacher.dtos.TeacherCourseOptionResponse;
import com.rewe.studentstrackingsystem.teacher.dtos.TeacherStudentOptionResponse;
import com.rewe.studentstrackingsystem.teacher.entity.Teacher;
import com.rewe.studentstrackingsystem.teacher.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TeacherWorkbenchService {

    private final TeacherRepository teacherRepository;
    private final CourseService courseService;
    private final StudentRepository studentRepository;
    private final AttendanceService attendanceService;
    private final GradeService gradeService;

    @Transactional(readOnly = true)
    public List<TeacherCourseOptionResponse> getCourses(String teacherUsername) {
        var teacher = getTeacherByUsername(teacherUsername);

        return courseService.getCoursesByTeacher(teacher.getId()).stream()
                .map(course -> new TeacherCourseOptionResponse(course.getId(), course.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TeacherStudentOptionResponse> getStudentsByCourse(String teacherUsername, UUID courseId) {
        var teacher = getTeacherByUsername(teacherUsername);

        var course = courseService.getCourseById(courseId);
        if (!course.getTeacher().getId().equals(teacher.getId())) {
            throw new ValidationException("You can only access students from your own courses");
        }

        return studentRepository.findDistinctByCoursesId(courseId).stream()
                .map(student -> new TeacherStudentOptionResponse(
                        student.getId(),
                        student.getUser().getFirstName(),
                        student.getUser().getLastName(),
                        student.getUser().getEmail()))
                .toList();
    }

    public AttendanceResponse addAttendance(String teacherUsername, AttendanceRequest request) {
        Objects.requireNonNull(request, "AttendanceRequest cannot be null");

        var teacher = getTeacherByUsername(teacherUsername);
        ensureTeacherOwnsCourseAndStudentIsEnrolled(teacher.getId(), request.courseId(), request.studentId());

        var normalizedRequest = new AttendanceRequest(
                request.attendanceDate(),
                request.isPresent(),
                request.studentId(),
                teacher.getId(),
                request.courseId()
        );

        var attendance = attendanceService.create(normalizedRequest);
        return attendanceService.toResponse(attendance);
    }

    public GradeResponse addGrade(String teacherUsername, GradeRequest request) {
        Objects.requireNonNull(request, "GradeRequest cannot be null");

        var teacher = getTeacherByUsername(teacherUsername);
        ensureTeacherOwnsCourseAndStudentIsEnrolled(teacher.getId(), request.course(), request.student());

        return gradeService.createResponse(request);
    }

    public AttendanceResponse updateAttendance(String teacherUsername, UUID attendanceId, AttendanceRequest request) {
        Objects.requireNonNull(request, "AttendanceRequest cannot be null");

        var teacher = getTeacherByUsername(teacherUsername);
        ensureTeacherOwnsCourseAndStudentIsEnrolled(teacher.getId(), request.courseId(), request.studentId());

        var normalizedRequest = new AttendanceRequest(
                request.attendanceDate(),
                request.isPresent(),
                request.studentId(),
                teacher.getId(),
                request.courseId()
        );

        return attendanceService.updateByTeacher(teacher.getId(), attendanceId, normalizedRequest);
    }

    public GradeResponse updateGrade(String teacherUsername, UUID gradeId, GradeRequest request) {
        Objects.requireNonNull(request, "GradeRequest cannot be null");

        var teacher = getTeacherByUsername(teacherUsername);
        ensureTeacherOwnsCourseAndStudentIsEnrolled(teacher.getId(), request.course(), request.student());

        return gradeService.updateByTeacher(teacher.getId(), gradeId, request);
    }

    private void ensureTeacherOwnsCourseAndStudentIsEnrolled(UUID teacherId, UUID courseId, UUID studentId) {
        var course = courseService.getCourseById(courseId);
        if (!course.getTeacher().getId().equals(teacherId)) {
            throw new ValidationException("You can only manage grades and attendance for your own courses");
        }

        var student = studentRepository.findById(studentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Student", studentId.toString()));

        var enrolled = student.getCourses().stream().anyMatch(c -> c.getId().equals(courseId));
        if (!enrolled) {
            throw new ValidationException("Student is not enrolled in the selected course");
        }
    }

    private Teacher getTeacherByUsername(String teacherUsername) {
        return teacherRepository.findByUserUsername(teacherUsername)
                .orElseThrow(() -> ResourceNotFoundException.of("Teacher", teacherUsername));
    }
}

