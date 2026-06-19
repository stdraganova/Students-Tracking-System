package com.rewe.studentstrackingsystem.grade.entity;

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
@Table(name = "grades")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Grade {

    @Id
    private UUID id;

    @Column(name = "grade", nullable = false)
    private Double grade;

    @Column(name = "creation_date")
    private LocalDate creationDate;

    @ManyToOne
    private Student student;

    @ManyToOne
    private Teacher teacher;

    @ManyToOne
    private Course course;
}
