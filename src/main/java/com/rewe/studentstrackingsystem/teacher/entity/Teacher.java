package com.rewe.studentstrackingsystem.teacher.entity;

import com.rewe.studentstrackingsystem.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "teachers")
@Data
public class Teacher{

    @Id
    private UUID id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false)
    private String email;

    @OneToOne
    private User user;
}