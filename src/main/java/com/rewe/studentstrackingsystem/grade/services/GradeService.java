package com.rewe.studentstrackingsystem.grade.services;

import com.rewe.studentstrackingsystem.course.repository.CourseRepository;
import com.rewe.studentstrackingsystem.exception.ResourceNotFoundException;
import com.rewe.studentstrackingsystem.exception.ValidationException;
import com.rewe.studentstrackingsystem.grade.dto.GradeRequest;
import com.rewe.studentstrackingsystem.grade.dto.GradeResponse;
import com.rewe.studentstrackingsystem.grade.entity.Grade;
import com.rewe.studentstrackingsystem.grade.mapper.GradeMapper;
import com.rewe.studentstrackingsystem.grade.repository.GradeRepository;
import com.rewe.studentstrackingsystem.student.entity.Student;
import com.rewe.studentstrackingsystem.student.repository.StudentRepository;
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
public class GradeService {

    private final GradeRepository gradeRepository;
    private final GradeMapper gradeMapper;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;

    public Grade create(GradeRequest gradeRequest) {
        Objects.requireNonNull(gradeRequest, "GradeRequest cannot be null");

        var student = studentRepository.findById(gradeRequest.student())
                .orElseThrow(() -> ResourceNotFoundException.of("Student", gradeRequest.student().toString()));

        return create(gradeRequest, student);
    }

    public GradeResponse createResponse(GradeRequest gradeRequest) {
        return gradeMapper.toResponse(create(gradeRequest));
    }

    public GradeResponse save(Grade grade) {
        Objects.requireNonNull(grade, "Grade cannot be null");

        var savedGrade = gradeRepository.save(grade);
        log.info("Grade saved successfully: {}", savedGrade.getId());
        return gradeMapper.toResponse(savedGrade);
    }

    public Grade create(GradeRequest gradeRequest, Student student) {
        Objects.requireNonNull(gradeRequest, "GradeRequest cannot be null");
        Objects.requireNonNull(student, "Student cannot be null");

        if (gradeRequest.grade() == null || gradeRequest.grade() < 0 || gradeRequest.grade() > 100) {
            throw new ValidationException("Grade must be between 0 and 100");
        }

        if (gradeRequest.creationDate() == null) {
            throw new ValidationException("Grade creation date cannot be null");
        }

        var course = courseRepository.findById(gradeRequest.course())
                .orElseThrow(() -> ResourceNotFoundException.of("Course", gradeRequest.course().toString()));

        var grade = Grade.builder()
                .grade(gradeRequest.grade())
                .creationDate(gradeRequest.creationDate())
                .student(student)
                .course(course)
                .build();

        var savedGrade = gradeRepository.save(grade);
        log.info("Grade created for student: {}, course: {}, grade value: {}",
                 student.getId(), course.getId(), gradeRequest.grade());

        return savedGrade;
    }

    public void delete(UUID gradeId) {
        Objects.requireNonNull(gradeId, "Grade ID cannot be null");

        if (!gradeRepository.existsById(gradeId)) {
            throw ResourceNotFoundException.of("Grade", gradeId.toString());
        }

        log.info("Deleting grade: {}", gradeId);
        gradeRepository.deleteById(gradeId);
    }

    public GradeResponse update(UUID gradeId, GradeRequest request) {
        Objects.requireNonNull(gradeId, "Grade ID cannot be null");
        Objects.requireNonNull(request, "GradeRequest cannot be null");

        var existingGrade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> ResourceNotFoundException.of("Grade", gradeId.toString()));

        return updateInternal(existingGrade, request);
    }

    public GradeResponse updateByTeacher(UUID teacherId, UUID gradeId, GradeRequest request) {
        Objects.requireNonNull(teacherId, "Teacher ID cannot be null");
        Objects.requireNonNull(gradeId, "Grade ID cannot be null");
        Objects.requireNonNull(request, "GradeRequest cannot be null");

        var existingGrade = gradeRepository.findByIdAndCourseTeacherId(gradeId, teacherId)
                .orElseThrow(() -> ResourceNotFoundException.of("Grade", gradeId.toString()));

        return updateInternal(existingGrade, request);
    }

    private GradeResponse updateInternal(Grade existingGrade, GradeRequest request) {
        if (request.grade() == null || request.grade() < 0 || request.grade() > 100) {
            throw new ValidationException("Grade must be between 0 and 100");
        }

        if (request.creationDate() == null) {
            throw new ValidationException("Grade creation date cannot be null");
        }

        var student = studentRepository.findById(request.student())
                .orElseThrow(() -> ResourceNotFoundException.of("Student", request.student().toString()));

        var course = courseRepository.findById(request.course())
                .orElseThrow(() -> ResourceNotFoundException.of("Course", request.course().toString()));

        existingGrade.setGrade(request.grade());
        existingGrade.setCreationDate(request.creationDate());
        existingGrade.setStudent(student);
        existingGrade.setCourse(course);

        var saved = gradeRepository.save(existingGrade);
        return gradeMapper.toResponse(saved);
    }
}
