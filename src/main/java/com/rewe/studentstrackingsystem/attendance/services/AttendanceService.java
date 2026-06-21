package com.rewe.studentstrackingsystem.attendance.services;

import com.rewe.studentstrackingsystem.attendance.dtos.AttendanceRequest;
import com.rewe.studentstrackingsystem.attendance.entity.Attendance;
import com.rewe.studentstrackingsystem.attendance.repository.AttendanceRepository;
import com.rewe.studentstrackingsystem.course.repository.CourseRepository;
import com.rewe.studentstrackingsystem.exception.ResourceNotFoundException;
import com.rewe.studentstrackingsystem.exception.ValidationException;
import com.rewe.studentstrackingsystem.student.entity.Student;
import com.rewe.studentstrackingsystem.teacher.repository.TeacherRepository;
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
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;

    public Attendance create(AttendanceRequest attendanceRequest, Student student) {
        Objects.requireNonNull(attendanceRequest, "AttendanceRequest cannot be null");
        Objects.requireNonNull(student, "Student cannot be null");

        var teacher = teacherRepository.findById(attendanceRequest.teacherId())
                .orElseThrow(() -> ResourceNotFoundException.of("Teacher", attendanceRequest.teacherId().toString()));

        var course = courseRepository.findById(attendanceRequest.courseId())
                .orElseThrow(() -> ResourceNotFoundException.of("Course", attendanceRequest.courseId().toString()));

        var attendance = new Attendance();
        attendance.setAttendanceDate(attendanceRequest.attendanceDate());
        attendance.setCourse(course);
        attendance.setStudent(student);
        attendance.setTeacher(teacher);
        attendance.setPresent(attendanceRequest.isPresent());

        var savedAttendance = attendanceRepository.save(attendance);
        log.info("Attendance record created for student: {}, course: {}, date: {}",
                 student.getId(), course.getId(), attendanceRequest.attendanceDate());

        return savedAttendance;
    }

    public void delete(UUID attendanceId) {
        Objects.requireNonNull(attendanceId, "Attendance ID cannot be null");

        if (!attendanceRepository.existsById(attendanceId)) {
            throw ResourceNotFoundException.of("Attendance", attendanceId.toString());
        }

        log.info("Deleting attendance: {}", attendanceId);
        attendanceRepository.deleteById(attendanceId);
    }
}
