package com.monew.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.monew.common.dto.CursorRequest;
import com.monew.common.dto.PageResponse;
import com.monew.domain.notification.dto.NotificationDto;
import com.monew.domain.notification.entity.Notification;
import com.monew.domain.notification.entity.Notification.ResourceType;
import com.monew.domain.notification.exception.NotificationNotFoundException;
import com.monew.domain.notification.mapper.NotificationMapper;
import com.monew.domain.notification.repository.NotificationRepository;
import com.monew.domain.user.entity.User;
import com.monew.domain.user.repository.UserRepository;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    private final NotificationMapper notificationMapper = new NotificationMapper();

    private NotificationService service;

    private User user;

    @BeforeEach
    void setUp() throws Exception {
        service = new NotificationService(notificationRepository, notificationMapper, userRepository);
        user = User.builder().email("u@u.com").nickname("유저").password("p").build();
        setId(user, UUID.randomUUID());
    }

    @Test
    void createForUser_알림_저장() {
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(notificationRepository.save(any(Notification.class)))
            .willAnswer(inv -> inv.getArgument(0));

        NotificationDto dto = service.createForUser(
            user.getId(), "test", ResourceType.COMMENT, UUID.randomUUID());

        assertThat(dto.content()).isEqualTo("test");
        assertThat(dto.resourceType()).isEqualTo(ResourceType.COMMENT);
        assertThat(dto.confirmed()).isFalse();
    }

    @Test
    void findUnconfirmed_커서_페이지() throws Exception {
        Notification n1 = Notification.builder()
            .user(user).content("1").resourceType(ResourceType.COMMENT)
            .resourceId(UUID.randomUUID()).build();
        setId(n1, UUID.randomUUID());

        given(notificationRepository.findFirstUnconfirmedPage(any(UUID.class), any(Pageable.class)))
            .willReturn(List.of(n1));

        PageResponse<NotificationDto> page = service.findUnconfirmed(
            user.getId(), new CursorRequest(null, 20));

        assertThat(page.content()).hasSize(1);
        assertThat(page.hasNext()).isFalse();
    }

    @Test
    void confirm_본인_알림_확인() throws Exception {
        Notification n = Notification.builder()
            .user(user).content("x").resourceType(ResourceType.INTEREST)
            .resourceId(UUID.randomUUID()).build();
        setId(n, UUID.randomUUID());

        given(notificationRepository.findById(n.getId())).willReturn(Optional.of(n));

        service.confirm(user.getId(), n.getId());

        assertThat(n.isConfirmed()).isTrue();
    }

    @Test
    void deleteConfirmedBefore_레포지토리에_위임() {
        Instant threshold = Instant.now().minus(7, ChronoUnit.DAYS);
        given(notificationRepository.deleteConfirmedBefore(threshold)).willReturn(5);

        int deleted = service.deleteConfirmedBefore(threshold);

        assertThat(deleted).isEqualTo(5);
    }

    @Test
    void findUnconfirmed_커서_있음_분기() {
        given(notificationRepository.findUnconfirmedPageAfter(any(UUID.class), any(Instant.class), any(Pageable.class)))
            .willReturn(List.of());

        PageResponse<NotificationDto> page = service.findUnconfirmed(
            user.getId(), new CursorRequest("2026-04-10T00:00:00Z", 20));

        assertThat(page.content()).isEmpty();
    }

    @Test
    void findUnconfirmed_잘못된_커서_무시() {
        given(notificationRepository.findFirstUnconfirmedPage(any(UUID.class), any(Pageable.class)))
            .willReturn(List.of());

        PageResponse<NotificationDto> page = service.findUnconfirmed(
            user.getId(), new CursorRequest("not-instant", 20));

        assertThat(page.content()).isEmpty();
    }

    @Test
    void confirmAll_레포지토리_위임() {
        given(notificationRepository.confirmAllByUserId(user.getId())).willReturn(7);

        int updated = service.confirmAll(user.getId());

        assertThat(updated).isEqualTo(7);
    }

    @Test
    void confirm_없는_알림_예외() {
        UUID id = UUID.randomUUID();
        given(notificationRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.confirm(user.getId(), id))
            .isInstanceOf(NotificationNotFoundException.class);
    }

    @Test
    void confirm_타인_알림_예외() throws Exception {
        Notification n = Notification.builder()
            .user(user).content("x").resourceType(ResourceType.INTEREST)
            .resourceId(UUID.randomUUID()).build();
        setId(n, UUID.randomUUID());

        given(notificationRepository.findById(n.getId())).willReturn(Optional.of(n));

        assertThatThrownBy(() -> service.confirm(UUID.randomUUID(), n.getId()))
            .isInstanceOf(NotificationNotFoundException.class);
    }

    private static void setId(Object target, UUID id) throws Exception {
        Class<?> current = target.getClass();
        while (current != null) {
            try {
                Field field = current.getDeclaredField("id");
                field.setAccessible(true);
                field.set(target, id);
                return;
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new IllegalStateException("id field not found");
    }
}
