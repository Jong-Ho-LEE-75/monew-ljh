package com.monew.domain.article.backup;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "monew.backup", name = "scheduler-enabled", havingValue = "true")
public class ArticleBackupScheduler {

    private final ArticleBackupService articleBackupService;

    @Scheduled(cron = "${monew.backup.cron:0 5 0 * * *}")
    public void runDailyBackup() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        try {
            int count = articleBackupService.backup(yesterday);
            log.info("일일 백업 완료 date={} count={}", yesterday, count);
        } catch (Exception e) {
            log.error("일일 백업 실패 date={}", yesterday, e);
        }
    }
}
