package com.rewe.studentstrackingsystem.student.services;

import com.rewe.studentstrackingsystem.attendance.dtos.AttendanceRequest;
import com.rewe.studentstrackingsystem.attendance.services.AttendanceService;
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

    public void delete(UUID studentId) {
        studentRepository.deleteById(studentId);
    }
}
