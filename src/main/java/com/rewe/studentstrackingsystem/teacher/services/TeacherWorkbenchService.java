package com.rewe.studentstrackingsystem.teacher.services;

import com.rewe.studentstrackingsystem.attendance.dtos.AttendanceRequest;
import com.rewe.studentstrackingsystem.attendance.dtos.AttendanceRow;
import com.rewe.studentstrackingsystem.attendance.dtos.AttendanceResponse;
import com.rewe.studentstrackingsystem.attendance.repository.AttendanceRepository;
import com.rewe.studentstrackingsystem.attendance.services.AttendanceService;
import com.rewe.studentstrackingsystem.course.services.CourseService;
import com.rewe.studentstrackingsystem.exception.ResourceNotFoundException;
import com.rewe.studentstrackingsystem.exception.ValidationException;
import com.rewe.studentstrackingsystem.grade.dto.GradeRequest;
import com.rewe.studentstrackingsystem.grade.dto.GradeRow;
import com.rewe.studentstrackingsystem.grade.dto.GradeResponse;
import com.rewe.studentstrackingsystem.grade.repository.GradeRepository;
import com.rewe.studentstrackingsystem.grade.services.GradeService;
import com.rewe.studentstrackingsystem.student.dto.StudentListRow;
import com.rewe.studentstrackingsystem.student.repository.StudentRepository;
import com.rewe.studentstrackingsystem.teacher.dtos.TeacherCourseOptionResponse;
import com.rewe.studentstrackingsystem.teacher.dtos.TeacherStudentOptionResponse;
import com.rewe.studentstrackingsystem.teacher.entity.Teacher;
import com.rewe.studentstrackingsystem.teacher.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
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
    private final GradeRepository gradeRepository;
    private final AttendanceRepository attendanceRepository;
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

    @Transactional(readOnly = true)
    public TeacherCourseStudentsView getCourseStudentsView(String teacherUsername, UUID courseId) {
        var teacher = getTeacherByUsername(teacherUsername);

        var course = courseService.getCourseById(courseId);
        if (!course.getTeacher().getId().equals(teacher.getId())) {
            throw new ValidationException("You can only access students from your own courses");
        }

        var students = studentRepository.findDistinctByCoursesId(courseId).stream()
                .map(s -> new StudentListRow(
                        s.getId().toString(),
                        s.getUser().getFirstName() + " " + s.getUser().getLastName(),
                        s.getUser().getEmail()))
                .sorted(Comparator.comparing(StudentListRow::fullName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        return new TeacherCourseStudentsView(course.getName(), students);
    }

    @Transactional(readOnly = true)
    public TeacherStudentDetailView getStudentDetailView(String teacherUsername, UUID courseId, UUID studentId) {
        var teacher = getTeacherByUsername(teacherUsername);

        var course = courseService.getCourseById(courseId);
        if (!course.getTeacher().getId().equals(teacher.getId())) {
            throw new ValidationException("You can only access students from your own courses");
        }

        var student = studentRepository.findById(studentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Student", studentId.toString()));

        var grades = gradeRepository.findByCourseIdOrderByCreationDateDesc(courseId).stream()
                .filter(g -> g.getStudent().getId().equals(studentId))
                .map(g -> new GradeRow(g.getId().toString(), g.getGrade(), g.getCreationDate()))
                .toList();

        var attendances = attendanceRepository
                .findByCourseIdAndTeacherIdOrderByAttendanceDateDesc(courseId, teacher.getId()).stream()
                .filter(a -> a.getStudent().getId().equals(studentId))
                .map(a -> new AttendanceRow(a.getId().toString(), a.getAttendanceDate(), a.isPresent()))
                .toList();

        var studentName = student.getUser().getFirstName() + " " + student.getUser().getLastName();
        return new TeacherStudentDetailView(course.getName(), studentName, grades, attendances);
    }

    public AttendanceResponse addAttendance(String teacherUsername, AttendanceRequest request) {
        Objects.requireNonNull(request, "AttendanceRequest cannot be null");
        return addAttendance(
                teacherUsername,
                request.courseId(),
                request.studentId(),
                request.attendanceDate(),
                request.isPresent()
        );
    }

    public AttendanceResponse addAttendance(String teacherUsername,
                                           UUID courseId,
                                           UUID studentId,
                                           LocalDate attendanceDate,
                                           boolean present) {
        var teacher = getTeacherByUsername(teacherUsername);
        ensureTeacherOwnsCourseAndStudentIsEnrolled(teacher.getId(), courseId, studentId);

        var normalizedRequest = new AttendanceRequest(
                attendanceDate,
                present,
                studentId,
                teacher.getId(),
                courseId
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
        return updateAttendance(
                teacherUsername,
                attendanceId,
                request.courseId(),
                request.studentId(),
                request.attendanceDate(),
                request.isPresent()
        );
    }

    public AttendanceResponse updateAttendance(String teacherUsername,
                                               UUID attendanceId,
                                               UUID courseId,
                                               UUID studentId,
                                               LocalDate attendanceDate,
                                               boolean present) {
        var teacher = getTeacherByUsername(teacherUsername);
        ensureTeacherOwnsCourseAndStudentIsEnrolled(teacher.getId(), courseId, studentId);

        var normalizedRequest = new AttendanceRequest(
                attendanceDate,
                present,
                studentId,
                teacher.getId(),
                courseId
        );

        return attendanceService.updateByTeacher(teacher.getId(), attendanceId, normalizedRequest);
    }

    public GradeResponse updateGrade(String teacherUsername, UUID gradeId, GradeRequest request) {
        Objects.requireNonNull(request, "GradeRequest cannot be null");

        var teacher = getTeacherByUsername(teacherUsername);
        ensureTeacherOwnsCourseAndStudentIsEnrolled(teacher.getId(), request.course(), request.student());

        return gradeService.updateByTeacher(teacher.getId(), gradeId, request);
    }

    public void deleteGrade(String teacherUsername, UUID courseId, UUID studentId, UUID gradeId) {
        var teacher = getTeacherByUsername(teacherUsername);

        var grade = gradeRepository.findByIdAndCourseIdAndCourseTeacherId(gradeId, courseId, teacher.getId())
                .orElseThrow(() -> ResourceNotFoundException.of("Grade", gradeId.toString()));

        if (!grade.getStudent().getId().equals(studentId)) {
            throw new ValidationException("Grade not found");
        }

        gradeService.delete(gradeId);
    }

    public void deleteAttendance(String teacherUsername, UUID courseId, UUID studentId, UUID attendanceId) {
        var teacher = getTeacherByUsername(teacherUsername);

        var attendance = attendanceRepository
                .findByIdAndCourseIdAndTeacherId(attendanceId, courseId, teacher.getId())
                .orElseThrow(() -> ResourceNotFoundException.of("Attendance", attendanceId.toString()));

        if (!attendance.getStudent().getId().equals(studentId)) {
            throw new ValidationException("Attendance record not found");
        }

        attendanceService.delete(attendanceId);
    }

    public record TeacherCourseStudentsView(String courseName, List<StudentListRow> students) {
    }

    public record TeacherStudentDetailView(String courseName,
                                           String studentName,
                                           List<GradeRow> grades,
                                           List<AttendanceRow> attendances) {
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

