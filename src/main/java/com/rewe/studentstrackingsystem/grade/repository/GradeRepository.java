package com.rewe.studentstrackingsystem.grade.repository;

import com.rewe.studentstrackingsystem.grade.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GradeRepository extends JpaRepository<Grade, UUID> {
	Optional<Grade> findByIdAndCourseTeacherId(UUID gradeId, UUID teacherId);
	Optional<Grade> findByIdAndCourseIdAndCourseTeacherId(UUID gradeId, UUID courseId, UUID teacherId);
	List<Grade> findByCourseIdOrderByCreationDateDesc(UUID courseId);
}
