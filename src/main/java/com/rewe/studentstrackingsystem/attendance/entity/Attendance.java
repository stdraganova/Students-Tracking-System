package com.rewe.studentstrackingsystem.attendance.entity;

import com.rewe.studentstrackingsystem.course.entity.Course;
import com.rewe.studentstrackingsystem.student.entity.Student;
import com.rewe.studentstrackingsystem.teacher.entity.Teacher;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "attendance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "is_present", nullable = false)
    private boolean isPresent;

    @ManyToOne
    @JoinColumn(name = "student_id", referencedColumnName = "id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "teacher_id", referencedColumnName = "id", nullable = false)
    private Teacher teacher;

    @ManyToOne
    @JoinColumn(name = "course_id", referencedColumnName = "id", nullable = false)
    private Course course;
}