package com.monew.domain.article.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.monew.domain.article.collector.ArticleCollector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.transaction.PlatformTransactionManager;

@ExtendWith(MockitoExtension.class)
class ArticleCollectionJobConfigTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private ArticleCollector articleCollector;

    @Mock
    private StepContribution stepContribution;

    @Mock
    private ChunkContext chunkContext;

    @Test
    void Job과_Step_빈_생성() {
        ArticleCollectionJobConfig config = new ArticleCollectionJobConfig(
            jobRepository, transactionManager, articleCollector);

        Job job = config.articleCollectionJob();
        Step step = config.articleCollectionStep();

        assertThat(job.getName()).isEqualTo(ArticleCollectionJobConfig.JOB_NAME);
        assertThat(step.getName()).isEqualTo(ArticleCollectionJobConfig.STEP_NAME);
        assertThat(step).isInstanceOf(TaskletStep.class);
    }

    @Test
    void Tasklet이_ArticleCollector_호출() throws Exception {
        given(articleCollector.collect())
            .willReturn(new ArticleCollector.CollectionResult(5, 3, 1));

        ArticleCollectionJobConfig config = new ArticleCollectionJobConfig(
            jobRepository, transactionManager, articleCollector);
        TaskletStep step = (TaskletStep) config.articleCollectionStep();
        Tasklet tasklet = extractTasklet(step);

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
        verify(articleCollector).collect();
    }

    private Tasklet extractTasklet(TaskletStep step) throws Exception {
        var field = TaskletStep.class.getDeclaredField("tasklet");
        field.setAccessible(true);
        return (Tasklet) field.get(step);
    }
}
