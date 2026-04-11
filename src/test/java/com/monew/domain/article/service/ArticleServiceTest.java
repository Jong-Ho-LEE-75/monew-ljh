package com.monew.domain.article.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

import com.monew.common.dto.CursorRequest;
import com.monew.common.dto.PageResponse;
import com.monew.domain.article.dto.ArticleDto;
import com.monew.domain.article.entity.Article;
import com.monew.domain.article.entity.ArticleView;
import com.monew.domain.article.exception.ArticleNotFoundException;
import com.monew.domain.article.mapper.ArticleMapper;
import com.monew.domain.article.repository.ArticleRepository;
import com.monew.domain.article.repository.ArticleViewRepository;
import com.monew.domain.user.entity.User;
import com.monew.domain.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ArticleViewRepository articleViewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ArticleMapper articleMapper;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ArticleService articleService;

    private Article newArticle(String title) {
        return Article.builder()
            .source("NAVER")
            .sourceUrl("https://news.test/" + title)
            .title(title)
            .summary("summary")
            .publishedAt(Instant.parse("2026-04-10T00:00:00Z"))
            .build();
    }

    private ArticleDto dto(Article article, boolean viewed) {
        return new ArticleDto(
            UUID.randomUUID(),
            article.getSource(),
            article.getSourceUrl(),
            article.getTitle(),
            article.getSummary(),
            article.getPublishedAt(),
            null,
            null,
            article.getViewCount(),
            viewed
        );
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        void 커서_응답_생성() {
            Article a = newArticle("a");
            Article b = newArticle("b");

            given(articleRepository.findPage(any(), any(), any(Pageable.class)))
                .willReturn(List.of(a, b));
            given(articleMapper.toDto(a, false)).willReturn(dto(a, false));
            given(articleMapper.toDto(b, false)).willReturn(dto(b, false));

            PageResponse<ArticleDto> result = articleService.findAll(
                null, new CursorRequest(null, 10), null);

            assertThat(result.content()).hasSize(2);
            assertThat(result.hasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("view")
    class View {

        @Test
        void 첫_조회_시_count_증가() {
            UUID articleId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Article article = newArticle("a");
            User user = User.builder().email("a@a.com").nickname("n").password("p12345").build();

            given(articleRepository.findById(articleId)).willReturn(Optional.of(article));
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(articleViewRepository.save(any(ArticleView.class)))
                .willAnswer(inv -> inv.getArgument(0));
            given(articleMapper.toDto(article, true)).willReturn(dto(article, true));

            articleService.view(articleId, userId);

            assertThat(article.getViewCount()).isEqualTo(1);
        }

        @Test
        void 중복_조회_시_count_증가_없음() {
            UUID articleId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Article article = newArticle("a");
            User user = User.builder().email("a@a.com").nickname("n").password("p12345").build();

            given(articleRepository.findById(articleId)).willReturn(Optional.of(article));
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            willThrow(new DataIntegrityViolationException("unique"))
                .given(articleViewRepository).save(any(ArticleView.class));
            given(articleMapper.toDto(article, true)).willReturn(dto(article, true));

            articleService.view(articleId, userId);

            assertThat(article.getViewCount()).isEqualTo(0);
        }

        @Test
        void 삭제된_기사_조회_예외() {
            UUID articleId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Article article = newArticle("a");
            article.softDelete();

            given(articleRepository.findById(articleId)).willReturn(Optional.of(article));

            assertThatThrownBy(() -> articleService.view(articleId, userId))
                .isInstanceOf(ArticleNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("softDelete")
    class SoftDelete {

        @Test
        void 삭제_플래그_설정() {
            UUID articleId = UUID.randomUUID();
            Article article = newArticle("a");
            given(articleRepository.findById(articleId)).willReturn(Optional.of(article));

            articleService.softDelete(articleId);

            assertThat(article.isDeleted()).isTrue();
        }
    }
}
