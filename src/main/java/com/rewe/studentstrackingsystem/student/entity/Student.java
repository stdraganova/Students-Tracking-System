package com.rewe.studentstrackingsystem.student.entity;

import com.rewe.studentstrackingsystem.attendance.entity.Attendance;
import com.rewe.studentstrackingsystem.course.entity.Course;
import com.rewe.studentstrackingsystem.grade.entity.Grade;
import com.rewe.studentstrackingsystem.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "students")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false)
    private String email;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Grade> grades;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Attendance> attendances;

    @ManyToMany
    private List <Course> courses;
}