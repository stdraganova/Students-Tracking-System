package com.rewe.studentstrackingsystem.course.services;

import com.rewe.studentstrackingsystem.course.dto.CourseRequest;
import com.rewe.studentstrackingsystem.course.dto.CourseResponse;
import com.rewe.studentstrackingsystem.course.mapper.CourseMapper;
import com.rewe.studentstrackingsystem.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;

    public CourseResponse save(CourseRequest courseRequest) {
        var savedCourse = courseRepository.save(courseMapper.toEntity(courseRequest));
        return courseMapper.toResponse(savedCourse);
    }

    public void delete(UUID courseId) {
        courseRepository.deleteById(courseId);
    }
}
