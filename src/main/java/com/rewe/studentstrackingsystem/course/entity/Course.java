package com.rewe.studentstrackingsystem.course.entity;

import com.rewe.studentstrackingsystem.student.entity.Student;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "courses")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToMany(mappedBy = "courses")
    private List<Student> students = new ArrayList<>();
}
