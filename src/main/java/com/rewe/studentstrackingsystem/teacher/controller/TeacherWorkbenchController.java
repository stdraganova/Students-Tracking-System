package com.rewe.studentstrackingsystem.teacher.controller;

import com.rewe.studentstrackingsystem.attendance.dtos.AttendanceRequest;
import com.rewe.studentstrackingsystem.grade.dto.GradeRequest;
import com.rewe.studentstrackingsystem.teacher.services.TeacherWorkbenchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mvc/teacher-workbench")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherWorkbenchController {

    private final TeacherWorkbenchService teacherWorkbenchService;

    @GetMapping("/courses")
    public ResponseEntity<Map<String, Object>> getMyCourses(@AuthenticationPrincipal UserDetails userDetails) {
        var courses = teacherWorkbenchService.getCourses(userDetails.getUsername());
        return ResponseEntity.ok(Map.of("courses", courses));
    }

    @GetMapping("/courses/{courseId}/students")
    public ResponseEntity<Map<String, Object>> getStudentsForCourse(@AuthenticationPrincipal UserDetails userDetails,
                                                                    @PathVariable UUID courseId) {
        var students = teacherWorkbenchService.getStudentsByCourse(userDetails.getUsername(), courseId);
        return ResponseEntity.ok(Map.of("students", students));
    }

    @PostMapping("/attendance")
    public ResponseEntity<Map<String, Object>> addAttendance(@AuthenticationPrincipal UserDetails userDetails,
                                                              @Valid @RequestBody AttendanceRequest request) {
        var attendance = teacherWorkbenchService.addAttendance(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("attendance", attendance));
    }

    @PostMapping("/grades")
    public ResponseEntity<Map<String, Object>> addGrade(@AuthenticationPrincipal UserDetails userDetails,
                                                         @Valid @RequestBody GradeRequest request) {
        var grade = teacherWorkbenchService.addGrade(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("grade", grade));
    }

    @PutMapping("/attendance/{attendanceId}")
    public ResponseEntity<Map<String, Object>> updateAttendance(@AuthenticationPrincipal UserDetails userDetails,
                                                                 @PathVariable UUID attendanceId,
                                                                 @Valid @RequestBody AttendanceRequest request) {
        var attendance = teacherWorkbenchService.updateAttendance(userDetails.getUsername(), attendanceId, request);
        return ResponseEntity.ok(Map.of("attendance", attendance, "message", "Attendance updated successfully"));
    }

    @PutMapping("/grades/{gradeId}")
    public ResponseEntity<Map<String, Object>> updateGrade(@AuthenticationPrincipal UserDetails userDetails,
                                                            @PathVariable UUID gradeId,
                                                            @Valid @RequestBody GradeRequest request) {
        var grade = teacherWorkbenchService.updateGrade(userDetails.getUsername(), gradeId, request);
        return ResponseEntity.ok(Map.of("grade", grade, "message", "Grade updated successfully"));
    }
}

