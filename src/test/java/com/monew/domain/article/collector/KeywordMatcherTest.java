package com.monew.domain.article.collector;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class KeywordMatcherTest {

    private CollectedArticle article(String title, String summary) {
        return new CollectedArticle("NAVER", "url", title, summary, Instant.now());
    }

    @Test
    void 제목_매칭() {
        assertThat(KeywordMatcher.matches(article("Spring 신규 기능", "x"), List.of("spring")))
            .isTrue();
    }

    @Test
    void 요약_매칭() {
        assertThat(KeywordMatcher.matches(article("x", "본문에 Java가 있음"), List.of("java")))
            .isTrue();
    }

    @Test
    void 매칭_없음() {
        assertThat(KeywordMatcher.matches(article("Python", "Django"), List.of("spring", "java")))
            .isFalse();
    }

    @Test
    void 빈_키워드는_false() {
        assertThat(KeywordMatcher.matches(article("Spring", "x"), List.of())).isFalse();
    }

    @Test
    void null_필드_안전() {
        assertThat(KeywordMatcher.matches(article(null, null), List.of("spring"))).isFalse();
    }
}
