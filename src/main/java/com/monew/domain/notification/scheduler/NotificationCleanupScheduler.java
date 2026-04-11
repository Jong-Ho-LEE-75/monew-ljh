package com.monew.domain.notification.scheduler;

import com.monew.domain.notification.service.NotificationService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "monew.notification.cleanup",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class NotificationCleanupScheduler {

    private final NotificationService notificationService;

    @Scheduled(cron = "${monew.notification.cleanup.cron:0 10 0 * * *}")
    public void purgeConfirmedNotifications() {
        Instant threshold = Instant.now().minus(7, ChronoUnit.DAYS);
        try {
            int deleted = notificationService.deleteConfirmedBefore(threshold);
            log.info("확인 후 7일 경과 알림 삭제 threshold={} count={}", threshold, deleted);
        } catch (Exception e) {
            log.error("알림 정리 배치 실패 threshold={}", threshold, e);
        }
    }
}
