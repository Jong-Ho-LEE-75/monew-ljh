package com.monew.domain.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.monew.domain.user.dto.UserDto;
import com.monew.domain.user.dto.request.UserLoginRequest;
import com.monew.domain.user.dto.request.UserPasswordChangeRequest;
import com.monew.domain.user.dto.request.UserRegisterRequest;
import com.monew.domain.user.dto.request.UserUpdateRequest;
import com.monew.domain.user.service.UserService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController controller;

    private UserDto sampleDto() {
        return new UserDto(UUID.randomUUID(), "a@a.com", "n", Instant.now());
    }

    @Test
    void register_201_반환() {
        UserRegisterRequest req = new UserRegisterRequest("a@a.com", "nick", "pass12");
        UserDto dto = sampleDto();
        given(userService.register(req)).willReturn(dto);

        ResponseEntity<UserDto> response = controller.register(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void login_200_반환() {
        UserLoginRequest req = new UserLoginRequest("a@a.com", "pass12");
        UserDto dto = sampleDto();
        given(userService.login(req)).willReturn(dto);

        ResponseEntity<UserDto> response = controller.login(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void find_조회() {
        UUID userId = UUID.randomUUID();
        UserDto dto = sampleDto();
        given(userService.findById(userId)).willReturn(dto);

        ResponseEntity<UserDto> response = controller.find(userId);

        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void update_닉네임() {
        UUID userId = UUID.randomUUID();
        UserUpdateRequest req = new UserUpdateRequest("새이름");
        UserDto dto = sampleDto();
        given(userService.update(userId, req)).willReturn(dto);

        ResponseEntity<UserDto> response = controller.update(userId, req);

        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void changePassword_200_반환() {
        UUID userId = UUID.randomUUID();
        UserPasswordChangeRequest req = new UserPasswordChangeRequest("oldPass", "newPass");
        UserDto dto = sampleDto();
        given(userService.changePassword(userId, req)).willReturn(dto);

        ResponseEntity<UserDto> response = controller.changePassword(userId, req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void softDelete_204() {
        UUID userId = UUID.randomUUID();

        ResponseEntity<Void> response = controller.softDelete(userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        then(userService).should(times(1)).softDelete(userId);
    }
}
