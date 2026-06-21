package com.rewe.studentstrackingsystem.course.services;

import com.rewe.studentstrackingsystem.course.dto.CourseRequest;
import com.rewe.studentstrackingsystem.course.dto.CourseResponse;
import com.rewe.studentstrackingsystem.course.entity.Course;
import com.rewe.studentstrackingsystem.course.mapper.CourseMapper;
import com.rewe.studentstrackingsystem.course.repository.CourseRepository;
import com.rewe.studentstrackingsystem.exception.ResourceNotFoundException;
import com.rewe.studentstrackingsystem.teacher.repository.TeacherRepository;
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
public class CourseService {

    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final CourseMapper courseMapper;

    public void save(Course course) {
        Objects.requireNonNull(course, "Course cannot be null");

        courseRepository.save(course);
        log.info("Course saved: {}", course.getId());
    }

    public CourseResponse create(CourseRequest courseRequest) {
        Objects.requireNonNull(courseRequest, "CourseRequest cannot be null");

        var newCourse = courseMapper.toEntity(courseRequest);
        Objects.requireNonNull(newCourse, "Course mapping failed");

        var teacher = teacherRepository.findById(courseRequest.teacherId())
                .orElseThrow(() -> ResourceNotFoundException.of("Teacher", courseRequest.teacherId().toString()));

        newCourse.setTeacher(teacher);
        teacher.getCourses().add(newCourse);

        teacherRepository.save(teacher);
        var savedCourse = courseRepository.save(newCourse);

        log.info("Course created: {} by teacher: {}", savedCourse.getId(), teacher.getId());
        return courseMapper.toResponse(savedCourse);
    }

    public Course getCourseById(UUID courseId) {
        Objects.requireNonNull(courseId, "Course ID cannot be null");

        return courseRepository.findById(courseId)
                .orElseThrow(() -> ResourceNotFoundException.of("Course", courseId.toString()));
    }

    public void delete(UUID courseId) {
        Objects.requireNonNull(courseId, "Course ID cannot be null");

        if (!courseRepository.existsById(courseId)) {
            throw ResourceNotFoundException.of("Course", courseId.toString());
        }

        log.info("Deleting course: {}", courseId);
        courseRepository.deleteById(courseId);
    }
}
