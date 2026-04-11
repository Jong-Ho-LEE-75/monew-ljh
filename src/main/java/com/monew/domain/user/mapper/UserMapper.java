package com.monew.domain.user.mapper;

import com.monew.domain.user.dto.UserDto;
import com.monew.domain.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);
}
