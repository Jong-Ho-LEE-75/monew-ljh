package com.monew.domain.user.controller;

import com.monew.domain.user.dto.UserDto;
import com.monew.domain.user.dto.request.UserLoginRequest;
import com.monew.domain.user.dto.request.UserPasswordChangeRequest;
import com.monew.domain.user.dto.request.UserRegisterRequest;
import com.monew.domain.user.dto.request.UserUpdateRequest;
import com.monew.domain.user.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> register(@Valid @RequestBody UserRegisterRequest request) {
        UserDto created = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@Valid @RequestBody UserLoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> find(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.findById(userId));
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserDto> update(
        @PathVariable UUID userId,
        @Valid @RequestBody UserUpdateRequest request
    ) {
        return ResponseEntity.ok(userService.update(userId, request));
    }

    @PutMapping("/{userId}/password")
    public ResponseEntity<UserDto> changePassword(
        @PathVariable UUID userId,
        @Valid @RequestBody UserPasswordChangeRequest request
    ) {
        return ResponseEntity.ok(userService.changePassword(userId, request));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> softDelete(@PathVariable UUID userId) {
        userService.softDelete(userId);
        return ResponseEntity.noContent().build();
    }
}
