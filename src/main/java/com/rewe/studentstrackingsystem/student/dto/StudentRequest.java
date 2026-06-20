package com.rewe.studentstrackingsystem.student.dto;

import com.rewe.studentstrackingsystem.user.entity.User;
import jakarta.validation.constraints.NotNull;

public record StudentRequest(
        @NotNull User user
) {
}
