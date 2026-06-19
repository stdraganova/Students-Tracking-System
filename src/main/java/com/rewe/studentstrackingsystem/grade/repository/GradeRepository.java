package com.rewe.studentstrackingsystem.grade.repository;

import com.rewe.studentstrackingsystem.grade.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GradeRepository extends JpaRepository<Grade, UUID> {
}
