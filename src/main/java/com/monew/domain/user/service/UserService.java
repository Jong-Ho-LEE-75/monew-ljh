package com.monew.domain.user.service;

import com.monew.domain.user.dto.UserDto;
import com.monew.domain.user.dto.request.UserLoginRequest;
import com.monew.domain.user.dto.request.UserRegisterRequest;
import com.monew.domain.user.dto.request.UserUpdateRequest;
import com.monew.domain.user.entity.User;
import com.monew.domain.user.exception.DuplicateUserException;
import com.monew.domain.user.exception.InvalidPasswordException;
import com.monew.domain.user.exception.UserNotFoundException;
import com.monew.domain.user.mapper.UserMapper;
import com.monew.domain.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserDto register(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateUserException(request.email());
        }

        User user = User.builder()
            .email(request.email())
            .nickname(request.nickname())
            .password(request.password())
            .build();

        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    public UserDto login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new UserNotFoundException(request.email()));

        if (user.isDeleted() || !user.matchPassword(request.password())) {
            throw new InvalidPasswordException(request.email());
        }

        return userMapper.toDto(user);
    }

    public UserDto findById(UUID userId) {
        return userMapper.toDto(getActiveUser(userId));
    }

    @Transactional
    public UserDto update(UUID userId, UserUpdateRequest request) {
        User user = getActiveUser(userId);
        user.updateNickname(request.nickname());
        return userMapper.toDto(user);
    }

    @Transactional
    public void softDelete(UUID userId) {
        User user = getActiveUser(userId);
        user.softDelete();
    }

    private User getActiveUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        if (user.isDeleted()) {
            throw new UserNotFoundException(userId);
        }
        return user;
    }
}
