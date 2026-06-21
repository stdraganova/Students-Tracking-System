package com.rewe.studentstrackingsystem.course.services;

import com.rewe.studentstrackingsystem.course.dto.CourseRequest;
import com.rewe.studentstrackingsystem.course.dto.CourseResponse;
import com.rewe.studentstrackingsystem.course.mapper.CourseMapper;
import com.rewe.studentstrackingsystem.course.repository.CourseRepository;
import com.rewe.studentstrackingsystem.teacher.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final CourseMapper courseMapper;

    public CourseResponse create(CourseRequest courseRequest) {
        var newCourse = courseMapper.toEntity(courseRequest);
        var teacher = teacherRepository.findById(courseRequest.teacherId())
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found with id: " + courseRequest.teacherId()));
        newCourse.setTeacher(teacher);
        teacher.getCourses().add(newCourse);

        teacherRepository.save(teacher);
        var savedCourse = courseRepository.save(newCourse);

        return courseMapper.toResponse(savedCourse);
    }

    public void delete(UUID courseId) {
        courseRepository.deleteById(courseId);
    }
}
