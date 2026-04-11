package com.monew.domain.article.batch;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

@ExtendWith(MockitoExtension.class)
class ArticleCollectionSchedulerTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job articleCollectionJob;

    @InjectMocks
    private ArticleCollectionScheduler scheduler;

    @Test
    void runHourly_Job_실행() throws Exception {
        scheduler.runHourly();

        then(jobLauncher).should(times(1)).run(eq(articleCollectionJob), any(JobParameters.class));
    }

    @Test
    void runHourly_예외_삼킴() throws Exception {
        willThrow(new RuntimeException("boom")).given(jobLauncher).run(eq(articleCollectionJob), any(JobParameters.class));

        scheduler.runHourly();
    }
}
