package com.monew.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.monew.domain.user.dto.UserDto;
import com.monew.domain.user.dto.request.UserLoginRequest;
import com.monew.domain.user.dto.request.UserPasswordChangeRequest;
import com.monew.domain.user.dto.request.UserRegisterRequest;
import com.monew.domain.user.dto.request.UserUpdateRequest;
import com.monew.domain.user.entity.User;
import com.monew.domain.user.exception.DuplicateUserException;
import com.monew.domain.user.exception.InvalidPasswordException;
import com.monew.domain.user.exception.UserNotFoundException;
import com.monew.domain.user.mapper.UserMapper;
import com.monew.domain.user.repository.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User newUser(String email, String nickname, String password) {
        return User.builder()
            .email(email)
            .nickname(nickname)
            .password(password)
            .build();
    }

    private UserDto toDto(User user) {
        return new UserDto(UUID.randomUUID(), user.getEmail(), user.getNickname(), Instant.now());
    }

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        void 정상_등록() {
            UserRegisterRequest request = new UserRegisterRequest("a@a.com", "nick", "pass12");
            User saved = newUser("a@a.com", "nick", "{bcrypt}hashed");
            UserDto dto = toDto(saved);

            given(userRepository.existsByEmail("a@a.com")).willReturn(false);
            given(passwordEncoder.encode("pass12")).willReturn("{bcrypt}hashed");
            given(userRepository.save(any(User.class))).willReturn(saved);
            given(userMapper.toDto(saved)).willReturn(dto);

            UserDto result = userService.register(request);

            assertThat(result.email()).isEqualTo("a@a.com");
            verify(passwordEncoder).encode("pass12");
            verify(userRepository).save(any(User.class));
        }

        @Test
        void 등록시_비밀번호는_해싱되어_저장() {
            UserRegisterRequest request = new UserRegisterRequest("a@a.com", "nick", "pass12");
            org.mockito.ArgumentCaptor<User> captor = org.mockito.ArgumentCaptor.forClass(User.class);

            given(userRepository.existsByEmail("a@a.com")).willReturn(false);
            given(passwordEncoder.encode("pass12")).willReturn("{bcrypt}hashed");
            given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));
            given(userMapper.toDto(any(User.class))).willReturn(
                new UserDto(UUID.randomUUID(), "a@a.com", "nick", Instant.now()));

            userService.register(request);

            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getPassword()).isEqualTo("{bcrypt}hashed");
            assertThat(captor.getValue().getPassword()).isNotEqualTo("pass12");
        }

        @Test
        void 이메일_중복_시_예외() {
            UserRegisterRequest request = new UserRegisterRequest("a@a.com", "nick", "pass12");
            given(userRepository.existsByEmail("a@a.com")).willReturn(true);

            assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(DuplicateUserException.class);

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        void 정상_로그인() {
            UserLoginRequest request = new UserLoginRequest("a@a.com", "pass12");
            User user = newUser("a@a.com", "nick", "{bcrypt}hashed");

            given(userRepository.findByEmail("a@a.com")).willReturn(Optional.of(user));
            given(passwordEncoder.matches("pass12", "{bcrypt}hashed")).willReturn(true);
            given(userMapper.toDto(user)).willReturn(toDto(user));

            UserDto result = userService.login(request);

            assertThat(result.email()).isEqualTo("a@a.com");
        }

        @Test
        void 비밀번호_불일치_시_예외() {
            UserLoginRequest request = new UserLoginRequest("a@a.com", "wrong!");
            User user = newUser("a@a.com", "nick", "{bcrypt}hashed");

            given(userRepository.findByEmail("a@a.com")).willReturn(Optional.of(user));
            given(passwordEncoder.matches("wrong!", "{bcrypt}hashed")).willReturn(false);

            assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(InvalidPasswordException.class);
        }

        @Test
        void 존재하지_않는_이메일_예외() {
            UserLoginRequest request = new UserLoginRequest("a@a.com", "pass12");
            given(userRepository.findByEmail("a@a.com")).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        void 삭제된_사용자_로그인_예외() {
            UserLoginRequest request = new UserLoginRequest("a@a.com", "pass12");
            User user = newUser("a@a.com", "nick", "pass12");
            user.softDelete();

            given(userRepository.findByEmail("a@a.com")).willReturn(Optional.of(user));

            assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(InvalidPasswordException.class);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        void 닉네임_변경() {
            UUID id = UUID.randomUUID();
            User user = newUser("a@a.com", "old", "pass12");
            UserUpdateRequest request = new UserUpdateRequest("new");

            given(userRepository.findById(id)).willReturn(Optional.of(user));
            given(userMapper.toDto(user)).willReturn(
                new UserDto(id, "a@a.com", "new", Instant.now())
            );

            UserDto result = userService.update(id, request);

            assertThat(user.getNickname()).isEqualTo("new");
            assertThat(result.nickname()).isEqualTo("new");
        }

        @Test
        void 존재하지_않는_사용자_수정_예외() {
            UUID id = UUID.randomUUID();
            given(userRepository.findById(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.update(id, new UserUpdateRequest("new")))
                .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("changePassword")
    class ChangePassword {

        @Test
        void 정상_변경_새_비밀번호_해싱_저장() {
            UUID id = UUID.randomUUID();
            User user = newUser("a@a.com", "nick", "{bcrypt}old");
            UserPasswordChangeRequest request =
                new UserPasswordChangeRequest("oldPass", "newPass");

            given(userRepository.findById(id)).willReturn(Optional.of(user));
            given(passwordEncoder.matches("oldPass", "{bcrypt}old")).willReturn(true);
            given(passwordEncoder.encode("newPass")).willReturn("{bcrypt}new");
            given(userMapper.toDto(user)).willReturn(toDto(user));

            userService.changePassword(id, request);

            assertThat(user.getPassword()).isEqualTo("{bcrypt}new");
            verify(passwordEncoder).encode("newPass");
        }

        @Test
        void 현재_비밀번호_불일치_예외() {
            UUID id = UUID.randomUUID();
            User user = newUser("a@a.com", "nick", "{bcrypt}old");
            UserPasswordChangeRequest request =
                new UserPasswordChangeRequest("wrong!", "newPass");

            given(userRepository.findById(id)).willReturn(Optional.of(user));
            given(passwordEncoder.matches("wrong!", "{bcrypt}old")).willReturn(false);

            assertThatThrownBy(() -> userService.changePassword(id, request))
                .isInstanceOf(InvalidPasswordException.class);

            assertThat(user.getPassword()).isEqualTo("{bcrypt}old");
            verify(passwordEncoder, never()).encode(any());
        }

        @Test
        void 존재하지_않는_사용자_예외() {
            UUID id = UUID.randomUUID();
            UserPasswordChangeRequest request =
                new UserPasswordChangeRequest("oldPass", "newPass");

            given(userRepository.findById(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.changePassword(id, request))
                .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        void 삭제된_사용자_예외() {
            UUID id = UUID.randomUUID();
            User user = newUser("a@a.com", "nick", "{bcrypt}old");
            user.softDelete();
            UserPasswordChangeRequest request =
                new UserPasswordChangeRequest("oldPass", "newPass");

            given(userRepository.findById(id)).willReturn(Optional.of(user));

            assertThatThrownBy(() -> userService.changePassword(id, request))
                .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("softDelete")
    class SoftDelete {

        @Test
        void 정상_삭제_플래그_설정() {
            UUID id = UUID.randomUUID();
            User user = newUser("a@a.com", "nick", "pass12");

            given(userRepository.findById(id)).willReturn(Optional.of(user));

            userService.softDelete(id);

            assertThat(user.isDeleted()).isTrue();
            assertThat(user.getDeletedAt()).isNotNull();
        }

        @Test
        void hardDeleteBefore_레포지토리_위임() {
            Instant threshold = Instant.now().minus(1, ChronoUnit.DAYS);
            given(userRepository.hardDeleteByDeletedAtBefore(threshold)).willReturn(2);

            int deleted = userService.hardDeleteBefore(threshold);

            assertThat(deleted).isEqualTo(2);
        }

        @Test
        void 이미_삭제된_사용자_예외() {
            UUID id = UUID.randomUUID();
            User user = newUser("a@a.com", "nick", "pass12");
            user.softDelete();

            given(userRepository.findById(id)).willReturn(Optional.of(user));

            assertThatThrownBy(() -> userService.softDelete(id))
                .isInstanceOf(UserNotFoundException.class);
        }
    }
}
