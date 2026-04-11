package com.monew.domain.article.batch;

import com.monew.domain.article.collector.ArticleCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ArticleCollectionJobConfig {

    public static final String JOB_NAME = "articleCollectionJob";
    public static final String STEP_NAME = "articleCollectionStep";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ArticleCollector articleCollector;

    @Bean
    public Job articleCollectionJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
            .start(articleCollectionStep())
            .build();
    }

    @Bean
    public Step articleCollectionStep() {
        return new StepBuilder(STEP_NAME, jobRepository)
            .tasklet((contribution, chunkContext) -> {
                ArticleCollector.CollectionResult result = articleCollector.collect();
                log.info("뉴스 수집 완료 attempted={} saved={} duplicated={}",
                    result.attempted(), result.saved(), result.duplicated());
                contribution.incrementReadCount();
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }
}
