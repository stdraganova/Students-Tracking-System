package com.rewe.studentstrackingsystem.student.services;

import com.rewe.studentstrackingsystem.attendance.dtos.AttendanceRequest;
import com.rewe.studentstrackingsystem.attendance.services.AttendanceService;
import com.rewe.studentstrackingsystem.student.dto.StudentRequest;
import com.rewe.studentstrackingsystem.student.dto.StudentResponse;
import com.rewe.studentstrackingsystem.student.entity.Student;
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

    public Student getById(UUID studentId) {
        return studentRepository.findById(studentId).orElseThrow(() ->
                new RuntimeException("Student not found with id: " + studentId)
        );
    }

    public void addAttendance(AttendanceRequest attendanceRequest) {
        var attendance = attendanceService.create(attendanceRequest);
        var student = getById(attendanceRequest.studentId());

        student.getAttendances().add(attendance);
        studentRepository.save(student);
    }

    public void removeAttendance(UUID attendanceId, UUID studentId) {
        var student = getById(studentId);

        student.getAttendances()
                .removeIf(attendance -> attendance.getId().equals(attendanceId));

        attendanceService.delete(attendanceId);
        studentRepository.save(student);
    }

    public void delete(UUID studentId) {
        studentRepository.deleteById(studentId);
    }
}
