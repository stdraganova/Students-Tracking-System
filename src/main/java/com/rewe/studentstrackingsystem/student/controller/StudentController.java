package com.rewe.studentstrackingsystem.student.controller;

import com.rewe.studentstrackingsystem.attendance.dtos.AttendanceRequest;
import com.rewe.studentstrackingsystem.grade.dto.GradeRequest;
import com.rewe.studentstrackingsystem.student.dto.StudentRequest;
import com.rewe.studentstrackingsystem.student.services.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mvc/students")
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody StudentRequest request) {
        var response = studentService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("student", response));
    }

    @PostMapping("/attendance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> addAttendance(@Valid @RequestBody AttendanceRequest request) {
        studentService.addAttendance(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Attendance added successfully"));
    }

    @DeleteMapping("/{studentId}/attendance/{attendanceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> removeAttendance(@PathVariable UUID studentId, @PathVariable UUID attendanceId) {
        studentService.removeAttendance(attendanceId, studentId);
        return ResponseEntity.ok(Map.of("message", "Attendance removed successfully"));
    }

    @PostMapping("/{studentId}/courses/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> addCourse(@PathVariable UUID studentId, @PathVariable UUID courseId) {
        studentService.addCourse(courseId, studentId);
        return ResponseEntity.ok(Map.of("message", "Course added successfully"));
    }

    @DeleteMapping("/{studentId}/courses/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> removeCourse(@PathVariable UUID studentId, @PathVariable UUID courseId) {
        studentService.removeCourse(courseId, studentId);
        return ResponseEntity.ok(Map.of("message", "Course removed successfully"));
    }

    @PostMapping("/grades")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> addGrade(@Valid @RequestBody GradeRequest request) {
        studentService.addGrade(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Grade added successfully"));
    }

    @DeleteMapping("/{studentId}/grades/{gradeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> removeGrade(@PathVariable UUID studentId, @PathVariable UUID gradeId) {
        studentService.removeGrade(studentId, gradeId);
        return ResponseEntity.ok(Map.of("message", "Grade removed successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable UUID id) {
        studentService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Student deleted successfully"));
    }
}

