package com.monew.domain.article.backup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.monew.domain.article.entity.Article;
import com.monew.domain.article.repository.ArticleRepository;
import com.monew.domain.interest.entity.Interest;
import com.monew.domain.interest.repository.InterestRepository;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArticleBackupServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private InterestRepository interestRepository;

    @Spy
    private ArticleBackupStorage storage = new InMemoryArticleBackupStorage();

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @InjectMocks
    private ArticleBackupService service;

    private Interest interest;

    @BeforeEach
    void setup() {
        interest = Interest.builder().name("기술").keywords(List.of("자바")).build();
        setField(interest, "id", UUID.randomUUID());
    }

    private Article newArticle(String title, Instant publishedAt) {
        Article article = Article.builder()
            .source("NAVER")
            .sourceUrl("https://news.test/" + title + "/" + UUID.randomUUID())
            .title(title)
            .summary("summary")
            .publishedAt(publishedAt)
            .interest(interest)
            .build();
        setField(article, "id", UUID.randomUUID());
        setField(article, "createdAt", publishedAt);
        return article;
    }

    private static void setField(Object target, String name, Object value) {
        Class<?> c = target.getClass();
        while (c != null) {
            try {
                Field f = c.getDeclaredField(name);
                f.setAccessible(true);
                f.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                c = c.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        throw new IllegalStateException("필드를 찾을 수 없습니다: " + name);
    }

    @Nested
    @DisplayName("backup")
    class Backup {

        @Test
        void 지정된_날짜_기사를_JSON으로_업로드() {
            LocalDate date = LocalDate.of(2026, 4, 10);
            Instant created = date.atStartOfDay().toInstant(ZoneOffset.UTC).plusSeconds(60);
            Article a = newArticle("t1", created);
            Article b = newArticle("t2", created.plusSeconds(10));

            given(articleRepository.findAllCreatedBetween(any(Instant.class), any(Instant.class)))
                .willReturn(List.of(a, b));

            int count = service.backup(date);

            assertThat(count).isEqualTo(2);
            assertThat(storage.download(date)).isPresent();
            assertThat(storage.listDates()).containsExactly(date);
        }

        @Test
        void 대상_기사가_없으면_빈_배열_업로드() {
            LocalDate date = LocalDate.of(2026, 4, 10);
            given(articleRepository.findAllCreatedBetween(any(Instant.class), any(Instant.class)))
                .willReturn(List.of());

            int count = service.backup(date);

            assertThat(count).isZero();
            assertThat(storage.download(date)).contains("[]");
        }
    }

    @Nested
    @DisplayName("restore")
    class Restore {

        @Test
        void 백업_없으면_0_반환() {
            int restored = service.restore(LocalDate.of(2026, 4, 10));

            assertThat(restored).isZero();
            verifyNoInteractions(interestRepository);
        }

        @Test
        void 중복_제외_후_신규만_저장() {
            LocalDate date = LocalDate.of(2026, 4, 10);
            Instant created = date.atStartOfDay().toInstant(ZoneOffset.UTC).plusSeconds(60);
            Article a = newArticle("복구1", created);
            Article b = newArticle("복구2", created.plusSeconds(10));

            given(articleRepository.findAllCreatedBetween(any(Instant.class), any(Instant.class)))
                .willReturn(List.of(a, b));
            service.backup(date);

            given(articleRepository.existsBySourceUrl(a.getSourceUrl())).willReturn(true);
            given(articleRepository.existsBySourceUrl(b.getSourceUrl())).willReturn(false);
            given(interestRepository.findById(interest.getId())).willReturn(Optional.of(interest));
            given(articleRepository.save(any(Article.class)))
                .willAnswer(inv -> inv.getArgument(0));

            int restored = service.restore(date);

            assertThat(restored).isEqualTo(1);
            verify(articleRepository).save(any(Article.class));
        }

        @Test
        void 역직렬화_실패시_예외() {
            LocalDate date = LocalDate.of(2026, 4, 10);
            storage.upload(date, "not-json-content");

            org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.restore(date))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("역직렬화 실패");
        }
    }

    @Nested
    @DisplayName("listBackupDates")
    class ListBackupDates {

        @Test
        void 저장소에서_반환() {
            LocalDate d1 = LocalDate.of(2026, 4, 8);
            LocalDate d2 = LocalDate.of(2026, 4, 9);
            storage.upload(d1, "[]");
            storage.upload(d2, "[]");

            assertThat(service.listBackupDates()).containsExactly(d2, d1);
        }
    }
}
