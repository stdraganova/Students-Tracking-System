package com.rewe.studentstrackingsystem.grade.entity;

import com.rewe.studentstrackingsystem.course.entity.Course;
import com.rewe.studentstrackingsystem.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "grades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "grade", nullable = false)
    private Double grade;

    @Column(name = "creation_date")
    private LocalDate creationDate;

    @ManyToOne
    @JoinColumn(name = "student_id", referencedColumnName = "id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "course_id", referencedColumnName = "id", nullable = false)
    private Course course;
}
