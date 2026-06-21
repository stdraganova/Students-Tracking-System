package com.rewe.studentstrackingsystem.student.services;

import com.rewe.studentstrackingsystem.attendance.dtos.AttendanceRequest;
import com.rewe.studentstrackingsystem.attendance.services.AttendanceService;
import com.rewe.studentstrackingsystem.course.services.CourseService;
import com.rewe.studentstrackingsystem.grade.dto.GradeRequest;
import com.rewe.studentstrackingsystem.grade.services.GradeService;
import com.rewe.studentstrackingsystem.student.dto.StudentRequest;
import com.rewe.studentstrackingsystem.student.dto.StudentResponse;
import com.rewe.studentstrackingsystem.student.mapper.StudentMapper;
import com.rewe.studentstrackingsystem.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper mapper;
    private final AttendanceService attendanceService;
    private final CourseService courseService;
    private final GradeService gradeService;

    public StudentResponse save (StudentRequest studentRequest) {
        var savedStudent = studentRepository.save(mapper.toEntity(studentRequest));
        return mapper.toResponse(savedStudent);
    }

    public void addAttendance(AttendanceRequest attendanceRequest) {
        var attendance = attendanceService.create(attendanceRequest);
        var student = studentRepository.findById(attendanceRequest.studentId()).orElseThrow(() ->
                new RuntimeException("Student not found with id: " + attendanceRequest.studentId())
        );

        student.getAttendances().add(attendance);
        studentRepository.save(student);
    }

    public void removeAttendance(UUID attendanceId, UUID studentId) {
        var student = studentRepository.findById(studentId).orElseThrow(() ->
                new RuntimeException("Student not found with id: " + studentId)
        );

        student.getAttendances()
                .removeIf(attendance -> attendance.getId().equals(attendanceId));

        attendanceService.delete(attendanceId);
        studentRepository.save(student);
    }

    public void addCourse(UUID courseId, UUID studentId) {
        var course = courseService.getCourseById(courseId);
        var student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));

        student.getCourses().add(course);
        course.getStudents().add(student);

        studentRepository.save(student);
        courseService.save(course);
    }

    public void removeCourse(UUID courseId, UUID studentId) {
        var course = courseService.getCourseById(courseId);
        var student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));

        student.getCourses().remove(course);
        course.getStudents().remove(student);

        studentRepository.save(student);
        courseService.save(course);
    }

    public void addGrade(GradeRequest gradeRequest) {
        var student = studentRepository.findById(gradeRequest.student()).orElseThrow(() ->
                new RuntimeException("Student not found with id: " + gradeRequest.student())
        );

        var grade = gradeService.create(gradeRequest, student);

        student.getGrades().add(grade);
        studentRepository.save(student);
    }

    public void removeGrade(UUID studentId, UUID gradeId) {
        var student = studentRepository.findById(studentId).orElseThrow(() ->
                new RuntimeException("Student not found with id: " + studentId)
        );

        student.getGrades().removeIf(grade -> grade.getId().equals(gradeId));
        gradeService.delete(gradeId);
        studentRepository.save(student);
    }

    public void delete(UUID studentId) {
        studentRepository.deleteById(studentId);
    }
}
