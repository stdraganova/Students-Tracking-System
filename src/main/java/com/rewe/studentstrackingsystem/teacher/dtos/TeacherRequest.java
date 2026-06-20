package com.rewe.studentstrackingsystem.teacher.dtos;

import com.rewe.studentstrackingsystem.user.entity.User;
import jakarta.validation.constraints.NotNull;

public record TeacherRequest(
        @NotNull User user
) {
}