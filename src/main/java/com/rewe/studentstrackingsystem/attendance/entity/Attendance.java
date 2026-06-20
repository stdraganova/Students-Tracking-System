package com.rewe.studentstrackingsystem.attendance.entity;

import com.rewe.studentstrackingsystem.course.entity.Course;
import com.rewe.studentstrackingsystem.student.entity.Student;
import com.rewe.studentstrackingsystem.teacher.entity.Teacher;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "attendance")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "is_present", nullable = false)
    private boolean isPresent;

    @ManyToOne
    private Student student;

    @ManyToOne
    private Teacher teacher;

    @ManyToOne
    private Course course;
}