package com.rewe.studentstrackingsystem.user.mapper;

import com.rewe.studentstrackingsystem.user.dto.UserRequest;
import com.rewe.studentstrackingsystem.user.dto.UserResponse;
import com.rewe.studentstrackingsystem.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity (UserRequest userRequest);

    UserResponse toResponse(User user);
}
