package com.rewe.studentstrackingsystem.attendance.services;

import com.rewe.studentstrackingsystem.attendance.dtos.AttendanceRequest;
import com.rewe.studentstrackingsystem.attendance.entity.Attendance;
import com.rewe.studentstrackingsystem.attendance.repository.AttendanceRepository;
import com.rewe.studentstrackingsystem.course.services.CourseService;
import com.rewe.studentstrackingsystem.student.services.StudentService;
import com.rewe.studentstrackingsystem.teacher.services.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentService studentService;
    private final TeacherService teacherService;
    private final CourseService courseService;

    public Attendance save (Attendance attendance) {
        return attendanceRepository.save(attendance);
    }

    public Attendance create (AttendanceRequest attendanceRequest){
        var student = studentService.getById(attendanceRequest.studentId());
        var teacher = teacherService.getById(attendanceRequest.teacherId());
        var course = courseService.getById(attendanceRequest.courseId());
        var attendanceDate = attendanceRequest.attendanceDate();

        var attendance = new Attendance();
        attendance.setAttendanceDate(attendanceDate);
        attendance.setCourse(course);
        attendance.setStudent(student);
        attendance.setTeacher(teacher);
        attendance.setPresent(attendanceRequest.isPresent());

        return save(attendance);
    }

    public void delete (UUID attendanceId) {
        attendanceRepository.deleteById(attendanceId);
    }
}
