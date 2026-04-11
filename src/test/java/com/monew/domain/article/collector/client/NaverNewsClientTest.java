package com.monew.domain.article.collector.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.monew.domain.article.collector.CollectedArticle;
import com.monew.domain.article.collector.config.NewsCollectionProperties;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class NaverNewsClientTest {

    @Test
    void API_키_없으면_빈_리스트() {
        NewsCollectionProperties properties = new NewsCollectionProperties(
            new NewsCollectionProperties.Naver("https://x", "", "", 10),
            List.of()
        );
        NaverNewsClient client = new NaverNewsClient(RestClient.create(), properties);

        assertThat(client.fetch("Spring")).isEmpty();
    }

    @Test
    void sourceName_상수_반환() {
        NewsCollectionProperties properties = new NewsCollectionProperties(null, List.of());
        NaverNewsClient client = new NaverNewsClient(RestClient.create(), properties);

        assertThat(client.sourceName()).isEqualTo("NAVER");
    }

    @Test
    void record_구조_검증() {
        NaverNewsClient.NaverSearchItem item = new NaverNewsClient.NaverSearchItem(
            "<b>Spring</b>&amp;Boot",
            "https://orig",
            "https://link",
            "<b>요약</b>",
            "Wed, 10 Apr 2026 09:00:00 +0900"
        );

        CollectedArticle collected = new CollectedArticle(
            "NAVER",
            item.originallink(),
            item.title().replaceAll("<[^>]*>", "").replace("&amp;", "&"),
            item.description().replaceAll("<[^>]*>", ""),
            java.time.Instant.parse("2026-04-10T00:00:00Z")
        );

        assertThat(collected.title()).isEqualTo("Spring&Boot");
        assertThat(collected.summary()).isEqualTo("요약");
        assertThat(collected.source()).isEqualTo("NAVER");
    }
}
