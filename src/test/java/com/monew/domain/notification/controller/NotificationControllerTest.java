package com.monew.domain.notification.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.monew.common.dto.CursorRequest;
import com.monew.common.dto.PageResponse;
import com.monew.domain.notification.dto.NotificationDto;
import com.monew.domain.notification.entity.Notification.ResourceType;
import com.monew.domain.notification.service.NotificationService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController controller;

    private NotificationDto sampleDto() {
        return new NotificationDto(UUID.randomUUID(), "알림", ResourceType.COMMENT,
            UUID.randomUUID(), false, Instant.now());
    }

    @Test
    void findUnconfirmed_위임() {
        UUID userId = UUID.randomUUID();
        PageResponse<NotificationDto> page = new PageResponse<>(List.of(sampleDto()), null, 1, false);
        given(notificationService.findUnconfirmed(eq(userId), any(CursorRequest.class)))
            .willReturn(page);

        ResponseEntity<PageResponse<NotificationDto>> response =
            controller.findUnconfirmed(userId, null, 10);

        assertThat(response.getBody()).isEqualTo(page);
    }

    @Test
    void confirm_204() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        ResponseEntity<Void> response = controller.confirm(userId, notificationId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        then(notificationService).should(times(1)).confirm(userId, notificationId);
    }

    @Test
    void confirmAll_업데이트_건수_반환() {
        UUID userId = UUID.randomUUID();
        given(notificationService.confirmAll(userId)).willReturn(3);

        ResponseEntity<Map<String, Integer>> response = controller.confirmAll(userId);

        assertThat(response.getBody()).containsEntry("updated", 3);
    }
}
