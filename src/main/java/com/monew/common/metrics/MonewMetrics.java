package com.monew.common.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class MonewMetrics {

    private static final String NS = "monew";

    private final Counter articlesSaved;
    private final Counter articlesDuplicated;
    private final Counter collectionFailures;
    private final Timer collectionTimer;

    private final Counter backupUploaded;
    private final Counter backupRestored;
    private final Counter backupFailures;

    private final Counter notificationsCreated;

    public MonewMetrics(MeterRegistry registry) {
        this.articlesSaved = Counter.builder(NS + ".articles.saved")
            .description("새로 저장된 기사 수")
            .register(registry);
        this.articlesDuplicated = Counter.builder(NS + ".articles.duplicated")
            .description("중복으로 스킵된 기사 수")
            .register(registry);
        this.collectionFailures = Counter.builder(NS + ".collection.failures")
            .description("뉴스 소스 호출 실패 수")
            .tag("reason", "source")
            .register(registry);
        this.collectionTimer = Timer.builder(NS + ".collection.duration")
            .description("뉴스 수집 배치 실행 시간")
            .register(registry);

        this.backupUploaded = Counter.builder(NS + ".backup.uploaded")
            .description("S3 백업 업로드 건수")
            .register(registry);
        this.backupRestored = Counter.builder(NS + ".backup.restored")
            .description("S3 백업 복구 건수")
            .register(registry);
        this.backupFailures = Counter.builder(NS + ".backup.failures")
            .description("백업 실패 수")
            .register(registry);

        this.notificationsCreated = Counter.builder(NS + ".notifications.created")
            .description("생성된 알림 수")
            .register(registry);
    }

    public void incrementArticlesSaved(long count) {
        if (count > 0) {
            articlesSaved.increment(count);
        }
    }

    public void incrementArticlesDuplicated(long count) {
        if (count > 0) {
            articlesDuplicated.increment(count);
        }
    }

    public void incrementCollectionFailure() {
        collectionFailures.increment();
    }

    public Timer collectionTimer() {
        return collectionTimer;
    }

    public void incrementBackupUploaded() {
        backupUploaded.increment();
    }

    public void incrementBackupRestored(long count) {
        if (count > 0) {
            backupRestored.increment(count);
        }
    }

    public void incrementBackupFailure() {
        backupFailures.increment();
    }

    public void incrementNotificationsCreated(long count) {
        if (count > 0) {
            notificationsCreated.increment(count);
        }
    }
}
