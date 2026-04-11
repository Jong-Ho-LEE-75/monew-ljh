package com.monew.domain.notification.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.monew.domain.notification.service.NotificationService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationCleanupSchedulerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationCleanupScheduler scheduler;

    @Test
    void purge_7일_이전_확인_알림_삭제() {
        given(notificationService.deleteConfirmedBefore(any(Instant.class))).willReturn(3);

        scheduler.purgeConfirmedNotifications();

        ArgumentCaptor<Instant> captor = ArgumentCaptor.forClass(Instant.class);
        verify(notificationService).deleteConfirmedBefore(captor.capture());
        Instant expected = Instant.now().minus(7, ChronoUnit.DAYS);
        assertThat(captor.getValue()).isCloseTo(expected, within(5_000));
    }

    private static org.assertj.core.data.TemporalUnitOffset within(long millis) {
        return new org.assertj.core.data.TemporalUnitWithinOffset(
            millis, ChronoUnit.MILLIS);
    }
}
