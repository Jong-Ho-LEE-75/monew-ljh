package com.monew.domain.article.backup;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArticleBackupSchedulerTest {

    @Mock
    private ArticleBackupService articleBackupService;

    @InjectMocks
    private ArticleBackupScheduler scheduler;

    @Test
    void runDailyBackup_정상() {
        given(articleBackupService.backup(any(LocalDate.class))).willReturn(5);

        scheduler.runDailyBackup();

        then(articleBackupService).should(times(1)).backup(any(LocalDate.class));
    }

    @Test
    void runDailyBackup_예외_삼킴() {
        willThrow(new RuntimeException("boom")).given(articleBackupService).backup(any(LocalDate.class));

        scheduler.runDailyBackup();
    }
}
