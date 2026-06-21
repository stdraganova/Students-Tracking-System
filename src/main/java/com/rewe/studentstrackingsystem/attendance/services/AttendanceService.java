package com.rewe.studentstrackingsystem.attendance.services;

import com.rewe.studentstrackingsystem.attendance.dtos.AttendanceRequest;
import com.rewe.studentstrackingsystem.attendance.entity.Attendance;
import com.rewe.studentstrackingsystem.attendance.repository.AttendanceRepository;
import com.rewe.studentstrackingsystem.course.repository.CourseRepository;
import com.rewe.studentstrackingsystem.student.entity.Student;
import com.rewe.studentstrackingsystem.student.repository.StudentRepository;
import com.rewe.studentstrackingsystem.teacher.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;

    public Attendance create (AttendanceRequest attendanceRequest, Student student) {
        var teacher = teacherRepository.findById(attendanceRequest.teacherId()).orElseThrow(() ->
                new RuntimeException("Teacher not found with id: " + attendanceRequest.teacherId())
        );

        var course = courseRepository.findById(attendanceRequest.courseId()).orElseThrow(() ->
                new RuntimeException("Course not found with id: " + attendanceRequest.courseId())
        );

        var attendanceDate = attendanceRequest.attendanceDate();

        var attendance = new Attendance();
        attendance.setAttendanceDate(attendanceDate);
        attendance.setCourse(course);
        attendance.setStudent(student);
        attendance.setTeacher(teacher);
        attendance.setPresent(attendanceRequest.isPresent());

        return attendanceRepository.save(attendance);
    }

    public void delete (UUID attendanceId) {
        attendanceRepository.deleteById(attendanceId);
    }
}
