package com.monew.domain.article.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.monew.domain.article.dto.ArticleSearchCondition;
import com.monew.domain.article.dto.ArticleSortBy;
import com.monew.domain.article.dto.SortDirection;
import com.monew.domain.article.entity.Article;
import com.monew.domain.interest.entity.Interest;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase
@ActiveProfiles("test")
@Import(JpaAuditingTestConfig.class)
class ArticleSpecificationsTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private jakarta.persistence.EntityManager em;

    private Interest interestA;
    private Interest interestB;
    private Article a1;
    private Article a2;
    private Article a3;
    private Article deleted;

    @BeforeEach
    void setUp() {
        interestA = Interest.builder().name("스프링").keywords(List.of("Spring")).build();
        interestB = Interest.builder().name("자바").keywords(List.of("Java")).build();
        em.persist(interestA);
        em.persist(interestB);

        a1 = Article.builder()
            .source("NAVER")
            .sourceUrl("https://x/1")
            .title("Spring 신기능")
            .summary("새로운 기능 요약")
            .publishedAt(Instant.parse("2026-04-01T00:00:00Z"))
            .interest(interestA)
            .build();
        a2 = Article.builder()
            .source("HANKYUNG")
            .sourceUrl("https://x/2")
            .title("Java 17")
            .summary("자바 릴리스")
            .publishedAt(Instant.parse("2026-04-05T00:00:00Z"))
            .interest(interestB)
            .build();
        a2.incrementViewCount();
        a2.incrementViewCount();
        a3 = Article.builder()
            .source("NAVER")
            .sourceUrl("https://x/3")
            .title("Kotlin")
            .summary("코틀린")
            .publishedAt(Instant.parse("2026-04-09T00:00:00Z"))
            .interest(interestA)
            .build();
        a3.incrementViewCount();
        a3.incrementCommentCount();
        a3.incrementCommentCount();
        a3.incrementCommentCount();
        deleted = Article.builder()
            .source("NAVER")
            .sourceUrl("https://x/4")
            .title("삭제됨")
            .summary("x")
            .publishedAt(Instant.parse("2026-04-10T00:00:00Z"))
            .interest(interestA)
            .build();
        deleted.softDelete();

        articleRepository.save(a1);
        articleRepository.save(a2);
        articleRepository.save(a3);
        articleRepository.save(deleted);
        em.flush();
    }

    @Test
    void deleted_제외() {
        Specification<Article> spec = ArticleSpecifications.build(
            new ArticleSearchCondition(null, null, null, null, null, null, null), null);

        List<Article> result = articleRepository.findAll(spec);

        assertThat(result).hasSize(3);
        assertThat(result).noneMatch(Article::isDeleted);
    }

    @Test
    void interestId_필터() {
        Specification<Article> spec = ArticleSpecifications.build(
            new ArticleSearchCondition(null, null, null, null, interestA.getId(), null, null), null);

        List<Article> result = articleRepository.findAll(spec);

        assertThat(result).extracting(Article::getTitle).containsExactlyInAnyOrder("Spring 신기능", "Kotlin");
    }

    @Test
    void keyword_제목_요약_부분일치() {
        Specification<Article> spec = ArticleSpecifications.build(
            new ArticleSearchCondition("자바", null, null, null, null, null, null), null);

        List<Article> result = articleRepository.findAll(spec);

        assertThat(result).extracting(Article::getTitle).containsExactly("Java 17");
    }

    @Test
    void sourceIn_필터() {
        Specification<Article> spec = ArticleSpecifications.build(
            new ArticleSearchCondition(null, List.of("HANKYUNG"), null, null, null, null, null), null);

        List<Article> result = articleRepository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSource()).isEqualTo("HANKYUNG");
    }

    @Test
    void publishedFrom_필터() {
        Specification<Article> spec = ArticleSpecifications.build(
            new ArticleSearchCondition(null, null,
                Instant.parse("2026-04-05T00:00:00Z"), null, null, null, null), null);

        List<Article> result = articleRepository.findAll(spec);

        assertThat(result).extracting(Article::getTitle).containsExactlyInAnyOrder("Java 17", "Kotlin");
    }

    @Test
    void publishedTo_필터() {
        Specification<Article> spec = ArticleSpecifications.build(
            new ArticleSearchCondition(null, null, null,
                Instant.parse("2026-04-05T00:00:00Z"), null, null, null), null);

        List<Article> result = articleRepository.findAll(spec);

        assertThat(result).extracting(Article::getTitle).containsExactly("Spring 신기능");
    }

    @Test
    void publishedAt_cursor_DESC() {
        Specification<Article> spec = ArticleSpecifications.build(
            new ArticleSearchCondition(null, null, null, null, null,
                ArticleSortBy.PUBLISHED_AT, SortDirection.DESC),
            Instant.parse("2026-04-09T00:00:00Z"));

        List<Article> result = articleRepository.findAll(spec,
            PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "publishedAt"))).getContent();

        assertThat(result).extracting(Article::getTitle).containsExactly("Java 17", "Spring 신기능");
    }

    @Test
    void viewCount_cursor_DESC() {
        Specification<Article> spec = ArticleSpecifications.build(
            new ArticleSearchCondition(null, null, null, null, null,
                ArticleSortBy.VIEW_COUNT, SortDirection.DESC),
            2L);

        List<Article> result = articleRepository.findAll(spec);

        assertThat(result).extracting(Article::getViewCount).allMatch(v -> v < 2);
    }

    @Test
    void commentCount_cursor_ASC() {
        Specification<Article> spec = ArticleSpecifications.build(
            new ArticleSearchCondition(null, null, null, null, null,
                ArticleSortBy.COMMENT_COUNT, SortDirection.ASC),
            0L);

        List<Article> result = articleRepository.findAll(spec);

        assertThat(result).extracting(Article::getCommentCount).allMatch(v -> v > 0);
    }
}
