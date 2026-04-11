package com.monew.domain.user.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.monew.domain.user.service.UserService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.assertj.core.data.TemporalUnitWithinOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserCleanupSchedulerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserCleanupScheduler scheduler;

    @Test
    void purge_1일_경과_논리삭제_사용자_완전삭제() {
        given(userService.hardDeleteBefore(any(Instant.class))).willReturn(2);

        scheduler.purgeSoftDeletedUsers();

        ArgumentCaptor<Instant> captor = ArgumentCaptor.forClass(Instant.class);
        verify(userService).hardDeleteBefore(captor.capture());
        Instant expected = Instant.now().minus(1, ChronoUnit.DAYS);
        assertThat(captor.getValue())
            .isCloseTo(expected, new TemporalUnitWithinOffset(5_000, ChronoUnit.MILLIS));
    }
}
