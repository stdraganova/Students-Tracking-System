package com.rewe.studentstrackingsystem.grade.services;

import com.rewe.studentstrackingsystem.course.repository.CourseRepository;
import com.rewe.studentstrackingsystem.exception.ResourceNotFoundException;
import com.rewe.studentstrackingsystem.grade.dto.GradeRequest;
import com.rewe.studentstrackingsystem.grade.dto.GradeResponse;
import com.rewe.studentstrackingsystem.grade.entity.Grade;
import com.rewe.studentstrackingsystem.grade.mapper.GradeMapper;
import com.rewe.studentstrackingsystem.grade.repository.GradeRepository;
import com.rewe.studentstrackingsystem.student.entity.Student;
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

    public GradeResponse save(Grade grade) {
        Objects.requireNonNull(grade, "Grade cannot be null");

        var savedGrade = gradeRepository.save(grade);
        log.info("Grade saved successfully: {}", savedGrade.getId());
        return gradeMapper.toResponse(savedGrade);
    }

    public Grade create(GradeRequest gradeRequest, Student student) {
        Objects.requireNonNull(gradeRequest, "GradeRequest cannot be null");
        Objects.requireNonNull(student, "Student cannot be null");

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
}
