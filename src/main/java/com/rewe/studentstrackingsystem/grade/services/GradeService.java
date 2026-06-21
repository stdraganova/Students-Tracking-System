package com.rewe.studentstrackingsystem.grade.services;

import com.rewe.studentstrackingsystem.course.repository.CourseRepository;
import com.rewe.studentstrackingsystem.grade.dto.GradeRequest;
import com.rewe.studentstrackingsystem.grade.dto.GradeResponse;
import com.rewe.studentstrackingsystem.grade.entity.Grade;
import com.rewe.studentstrackingsystem.grade.mapper.GradeMapper;
import com.rewe.studentstrackingsystem.grade.repository.GradeRepository;
import com.rewe.studentstrackingsystem.student.entity.Student;
import com.rewe.studentstrackingsystem.student.repository.StudentRepository;
import com.rewe.studentstrackingsystem.teacher.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final GradeRepository gradeRepository;
    private final GradeMapper gradeMapper;
    private final CourseRepository courseRepository;

    public GradeResponse save(Grade grade) {
        var savedGrade = gradeRepository.save(grade);

        return gradeMapper.toResponse(savedGrade);
    }

    public Grade create (GradeRequest gradeRequest, Student student) {
        var course = courseRepository.findById(gradeRequest.course()).orElseThrow(() ->
                new RuntimeException("Course not found with id: " + gradeRequest.course())
        );

        var grade = Grade.builder()
                .grade(gradeRequest.grade())
                .creationDate(gradeRequest.creationDate())
                .student(student)
                .course(course)
                .build();

        gradeRepository.save(grade);
        return grade;
    }

    public void delete(UUID gradeId) {
        gradeRepository.deleteById(gradeId);
    }
}
