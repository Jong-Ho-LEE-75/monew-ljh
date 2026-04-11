package com.monew.domain.article.batch;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "monew.news", name = "scheduler-enabled", havingValue = "true", matchIfMissing = false)
public class ArticleCollectionScheduler {

    private final JobLauncher jobLauncher;
    private final Job articleCollectionJob;

    @Scheduled(cron = "${monew.news.cron:0 0 * * * *}")
    public void runHourly() {
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("runAt", Instant.now().toEpochMilli())
                .toJobParameters();
            jobLauncher.run(articleCollectionJob, params);
        } catch (Exception e) {
            log.error("뉴스 수집 Job 실행 실패", e);
        }
    }
}
