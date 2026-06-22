package com.rewe.studentstrackingsystem.attendance.repository;

import com.rewe.studentstrackingsystem.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
	Optional<Attendance> findByIdAndTeacherId(UUID attendanceId, UUID teacherId);
}
