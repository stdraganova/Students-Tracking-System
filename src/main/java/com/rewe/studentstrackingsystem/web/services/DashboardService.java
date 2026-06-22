package com.rewe.studentstrackingsystem.web.services;

import com.rewe.studentstrackingsystem.exception.ResourceNotFoundException;
import com.rewe.studentstrackingsystem.user.entity.Role;
import com.rewe.studentstrackingsystem.user.entity.User;
import com.rewe.studentstrackingsystem.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final UserRepository userRepository;

    public DashboardView getDashboard(String username) {
        var user = getUserByUsername(username);

        if (user.getRole() == Role.STUDENT && user.getStudent() != null) {
            var student = user.getStudent();

            var courses = student.getCourses().stream()
                    .map(course -> new StudentCourseRow(
                            course.getId(),
                            course.getName(),
                            fullName(course.getTeacher().getUser().getFirstName(), course.getTeacher().getUser().getLastName())))
                    .sorted(Comparator.comparing(StudentCourseRow::courseName, String.CASE_INSENSITIVE_ORDER))
                    .toList();

            var grades = student.getGrades().stream()
                    .map(grade -> new GradeRow(
                            grade.getId(),
                            grade.getCourse().getName(),
                            grade.getGrade(),
                            grade.getCreationDate()))
                    .sorted(Comparator.comparing(GradeRow::creationDate, Comparator.nullsLast(Comparator.reverseOrder())))
                    .toList();

            var attendances = student.getAttendances().stream()
                    .map(attendance -> new AttendanceRow(
                            attendance.getId(),
                            attendance.getAttendanceDate(),
                            attendance.isPresent(),
                            attendance.getCourse().getName(),
                            fullName(attendance.getTeacher().getUser().getFirstName(), attendance.getTeacher().getUser().getLastName())))
                    .sorted(Comparator.comparing(AttendanceRow::attendanceDate, Comparator.nullsLast(Comparator.reverseOrder())))
                    .toList();

            return new DashboardView(
                    user.getId(),
                    user.getUsername(),
                    fullName(user.getFirstName(), user.getLastName()),
                    user.getEmail(),
                    user.getRole(),
                    courses,
                    grades,
                    attendances,
                    List.of());
        }

        if (user.getRole() == Role.TEACHER && user.getTeacher() != null) {
            var teacherCourses = user.getTeacher().getCourses().stream()
                    .map(course -> new TeacherCourseRow(
                            course.getId(),
                            course.getName(),
                            course.getStudents().size()))
                    .sorted(Comparator.comparing(TeacherCourseRow::courseName, String.CASE_INSENSITIVE_ORDER))
                    .toList();

            return new DashboardView(
                    user.getId(),
                    user.getUsername(),
                    fullName(user.getFirstName(), user.getLastName()),
                    user.getEmail(),
                    user.getRole(),
                    List.of(),
                    List.of(),
                    List.of(),
                    teacherCourses);
        }

        return new DashboardView(
                user.getId(),
                user.getUsername(),
                fullName(user.getFirstName(), user.getLastName()),
                user.getEmail(),
                user.getRole(),
                List.of(),
                List.of(),
                List.of(),
                List.of());
    }

    public ProfileView getProfile(String username) {
        var user = getUserByUsername(username);
        return new ProfileView(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getProfilePictureUrl());
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> ResourceNotFoundException.of("User", username));
    }

    private String fullName(String firstName, String lastName) {
        return firstName + " " + lastName;
    }

    public record DashboardView(
            UUID userId,
            String username,
            String fullName,
            String email,
            Role role,
            List<StudentCourseRow> studentCourses,
            List<GradeRow> grades,
            List<AttendanceRow> attendances,
            List<TeacherCourseRow> teacherCourses
    ) {
    }

    public record StudentCourseRow(UUID id, String courseName, String teacherName) {
    }

    public record TeacherCourseRow(UUID id, String courseName, int enrolledStudents) {
    }

    public record GradeRow(UUID id, String courseName, Double value, LocalDate creationDate) {
    }

    public record AttendanceRow(UUID id, LocalDate attendanceDate, boolean present, String courseName, String teacherName) {
    }

    public record ProfileView(
            UUID id,
            String username,
            String firstName,
            String lastName,
            String email,
            Role role,
            String profilePictureUrl
    ) {
    }
}

