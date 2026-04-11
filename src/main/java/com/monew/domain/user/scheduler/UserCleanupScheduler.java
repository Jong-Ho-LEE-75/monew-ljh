package com.monew.domain.user.scheduler;

import com.monew.domain.user.service.UserService;
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
    prefix = "monew.user.cleanup",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class UserCleanupScheduler {

    private final UserService userService;

    @Scheduled(cron = "${monew.user.cleanup.cron:0 20 0 * * *}")
    public void purgeSoftDeletedUsers() {
        Instant threshold = Instant.now().minus(1, ChronoUnit.DAYS);
        try {
            int deleted = userService.hardDeleteBefore(threshold);
            log.info("논리 삭제 1일 경과 사용자 완전 삭제 threshold={} count={}", threshold, deleted);
        } catch (Exception e) {
            log.error("사용자 정리 배치 실패 threshold={}", threshold, e);
        }
    }
}
