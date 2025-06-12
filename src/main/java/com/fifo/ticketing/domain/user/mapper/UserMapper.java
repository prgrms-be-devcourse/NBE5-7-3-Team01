package com.fifo.ticketing.domain.user.mapper;

import com.fifo.ticketing.domain.user.dto.UserDto;
import com.fifo.ticketing.domain.user.entity.User;
import org.springframework.data.domain.Page;

public class UserMapper {

    private UserMapper() {
    }

    public static UserDto toUserDto(User user) {
        return new UserDto (
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.isBlocked(),
            user.getRole()
            );
    }

    public static Page<UserDto> toUserDtoPage(Page<User> users) {
        return users.map(UserMapper::toUserDto);
    }

}
