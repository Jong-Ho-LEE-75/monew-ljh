package com.monew.domain.article.collector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.monew.domain.article.entity.Article;
import com.monew.domain.article.repository.ArticleRepository;
import com.monew.domain.interest.entity.Interest;
import com.monew.domain.interest.repository.InterestRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ArticleCollectorTest {

    @Mock
    private NewsSourceClient client;

    @Mock
    private InterestRepository interestRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ArticleCollector collector(NewsSourceClient... clients) {
        return new ArticleCollector(List.of(clients), interestRepository, articleRepository, eventPublisher);
    }

    private CollectedArticle candidate(String title, String url) {
        return new CollectedArticle("NAVER", url, title, "summary", Instant.now());
    }

    @Test
    void 키워드_매칭된_기사만_저장() {
        Interest interest = Interest.builder().name("Spring").keywords(List.of("spring")).build();

        given(interestRepository.findAll()).willReturn(List.of(interest));
        given(client.fetch("Spring")).willReturn(List.of(
            candidate("Spring 소식", "url1"),
            candidate("Python 소식", "url2")
        ));
        given(articleRepository.existsBySourceUrl(anyString())).willReturn(false);
        given(articleRepository.save(any(Article.class))).willAnswer(inv -> inv.getArgument(0));

        ArticleCollector articleCollector = collector(client);

        ArticleCollector.CollectionResult result = articleCollector.collect();

        assertThat(result.saved()).isEqualTo(1);
        assertThat(result.duplicated()).isEqualTo(0);
        verify(articleRepository).save(any(Article.class));
    }

    @Test
    void 중복_URL은_저장_스킵() {
        Interest interest = Interest.builder().name("Spring").keywords(List.of("spring")).build();

        given(interestRepository.findAll()).willReturn(List.of(interest));
        given(client.fetch("Spring")).willReturn(List.of(candidate("Spring 소식", "url1")));
        given(articleRepository.existsBySourceUrl("url1")).willReturn(true);

        ArticleCollector articleCollector = collector(client);

        ArticleCollector.CollectionResult result = articleCollector.collect();

        assertThat(result.saved()).isEqualTo(0);
        assertThat(result.duplicated()).isEqualTo(1);
        verify(articleRepository, never()).save(any());
    }

    @Test
    void 키워드_없는_관심사는_스킵() {
        Interest interest = Interest.builder().name("empty").keywords(List.of()).build();

        given(interestRepository.findAll()).willReturn(List.of(interest));

        ArticleCollector articleCollector = collector(client);

        ArticleCollector.CollectionResult result = articleCollector.collect();

        assertThat(result.attempted()).isEqualTo(0);
        assertThat(result.saved()).isEqualTo(0);
    }

    @Test
    void 클라이언트_예외는_로그만_남기고_계속() {
        Interest interest = Interest.builder().name("Spring").keywords(List.of("spring")).build();

        given(interestRepository.findAll()).willReturn(List.of(interest));
        given(client.fetch(anyString())).willThrow(new RuntimeException("API down"));

        ArticleCollector articleCollector = collector(client);

        ArticleCollector.CollectionResult result = articleCollector.collect();

        assertThat(result.saved()).isEqualTo(0);
        verify(articleRepository, never()).save(any());
    }
}
