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

import java.util.List;
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
        var savedCourse = courseRepository.save(newCourse);

        teacher.getCourses().add(savedCourse);

        log.info("Course created: {} by teacher: {}", savedCourse.getId(), teacher.getId());
        return courseMapper.toResponse(savedCourse);
    }

    public Course getCourseById(UUID courseId) {
        Objects.requireNonNull(courseId, "Course ID cannot be null");

        return courseRepository.findById(courseId)
                .orElseThrow(() -> ResourceNotFoundException.of("Course", courseId.toString()));
    }

    @Transactional(readOnly = true)
    public List<Course> getCoursesByTeacher(UUID teacherId) {
        Objects.requireNonNull(teacherId, "Teacher ID cannot be null");
        return courseRepository.findByTeacherId(teacherId);
    }

    public CourseResponse update(UUID courseId, CourseRequest request) {
        Objects.requireNonNull(courseId, "Course ID cannot be null");
        Objects.requireNonNull(request, "CourseRequest cannot be null");

        var course = courseRepository.findById(courseId)
                .orElseThrow(() -> ResourceNotFoundException.of("Course", courseId.toString()));

        course.setName(request.name());

        if (request.teacherId() != null && (course.getTeacher() == null || !request.teacherId().equals(course.getTeacher().getId()))) {
            var teacher = teacherRepository.findById(request.teacherId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Teacher", request.teacherId().toString()));

            if (course.getTeacher() != null) {
                course.getTeacher().getCourses().remove(course);
            }

            course.setTeacher(teacher);
            teacher.getCourses().add(course);
        }

        var updatedCourse = courseRepository.save(course);
        return courseMapper.toResponse(updatedCourse);
    }

    public void delete(UUID courseId) {
        Objects.requireNonNull(courseId, "Course ID cannot be null");

        if (!courseRepository.existsById(courseId)) {
            throw ResourceNotFoundException.of("Course", courseId.toString());
        }

        log.info("Deleting course: {}", courseId);
        courseRepository.deleteById(courseId);
    }

    @Transactional(readOnly = true)
    public List<Course> getAll() {
        return courseRepository.findAll();
    }
}
