package com.rewe.studentstrackingsystem.attendance.repository;

import com.rewe.studentstrackingsystem.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
	Optional<Attendance> findByIdAndTeacherId(UUID attendanceId, UUID teacherId);
	Optional<Attendance> findByIdAndCourseIdAndTeacherId(UUID attendanceId, UUID courseId, UUID teacherId);
	List<Attendance> findByCourseIdAndTeacherIdOrderByAttendanceDateDesc(UUID courseId, UUID teacherId);
}
