package com.rewe.studentstrackingsystem.course.entity;

import com.rewe.studentstrackingsystem.student.entity.Student;
import com.rewe.studentstrackingsystem.teacher.entity.Teacher;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "teacher_id", referencedColumnName = "id", nullable = false)
    private Teacher teacher;

    @ManyToMany(mappedBy = "courses")
    private List<Student> students = new ArrayList<>();
}
