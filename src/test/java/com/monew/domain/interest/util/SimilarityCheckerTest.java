package com.monew.domain.interest.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SimilarityCheckerTest {

    @Test
    void 동일_문자열은_유사도_1() {
        assertThat(SimilarityChecker.similarity("Spring", "Spring")).isEqualTo(1.0);
    }

    @Test
    void 대소문자_무시() {
        assertThat(SimilarityChecker.similarity("Spring", "spring")).isEqualTo(1.0);
    }

    @Test
    void 한_글자_차이는_임계값_초과() {
        assertThat(SimilarityChecker.isDuplicate("Spring", "Sprint")).isTrue();
    }

    @Test
    void 완전히_다른_단어는_중복_아님() {
        assertThat(SimilarityChecker.isDuplicate("Spring", "GoLang")).isFalse();
    }

    @Test
    void 경계값_80퍼센트_테스트() {
        assertThat(SimilarityChecker.similarity("abcde", "abcdX")).isEqualTo(0.8);
        assertThat(SimilarityChecker.isDuplicate("abcde", "abcdX")).isTrue();
    }

    @Test
    void null_입력은_0() {
        assertThat(SimilarityChecker.similarity(null, "abc")).isEqualTo(0.0);
        assertThat(SimilarityChecker.similarity("abc", null)).isEqualTo(0.0);
    }
}
