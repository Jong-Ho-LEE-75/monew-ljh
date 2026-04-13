package com.monew.domain.article.collector.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.monew.domain.article.collector.CollectedArticle;
import com.monew.domain.article.collector.config.NewsCollectionProperties;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
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

    @Test
    void fetch_정상_응답_파싱() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        String json = """
            {
              "items": [
                {
                  "title": "<b>Spring</b> 6 출시",
                  "originallink": "https://orig/1",
                  "link": "https://naver/1",
                  "description": "<b>Spring</b>&quot;ver6&quot;",
                  "pubDate": "Wed, 10 Apr 2026 09:00:00 +0900"
                },
                {
                  "title": "Java 25",
                  "originallink": "",
                  "link": "https://naver/2",
                  "description": "릴리스",
                  "pubDate": "not-a-date"
                }
              ]
            }
            """;

        server.expect(requestTo(Matchers.startsWith("https://openapi.naver.com/v1/search/news.json")))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("X-Naver-Client-Id", "cid"))
            .andExpect(header("X-Naver-Client-Secret", "csec"))
            .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        NewsCollectionProperties properties = new NewsCollectionProperties(
            new NewsCollectionProperties.Naver(
                "https://openapi.naver.com/v1/search/news.json",
                "cid", "csec", 10),
            List.of()
        );
        NaverNewsClient client = new NaverNewsClient(builder.build(), properties);

        List<CollectedArticle> result = client.fetch("Spring");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).title()).isEqualTo("Spring 6 출시");
        assertThat(result.get(0).summary()).isEqualTo("Spring\"ver6\"");
        assertThat(result.get(0).sourceUrl()).isEqualTo("https://orig/1");
        assertThat(result.get(1).sourceUrl()).isEqualTo("https://naver/2");
        assertThat(result.get(1).publishedAt()).isNotNull();
        server.verify();
    }

    @Test
    void fetch_items_null_빈_리스트() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        server.expect(requestTo(Matchers.startsWith("https://openapi.naver.com/v1/search/news.json")))
            .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        NewsCollectionProperties properties = new NewsCollectionProperties(
            new NewsCollectionProperties.Naver(
                "https://openapi.naver.com/v1/search/news.json",
                "cid", "csec", 0),
            List.of()
        );
        NaverNewsClient client = new NaverNewsClient(builder.build(), properties);

        assertThat(client.fetch("Spring")).isEmpty();
    }

    @Test
    void fetch_한글_쿼리_인코딩_정상() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        String json = """
            {
              "items": [
                {
                  "title": "인공지능 뉴스",
                  "originallink": "https://orig/1",
                  "link": "https://naver/1",
                  "description": "AI 관련 뉴스",
                  "pubDate": "Wed, 10 Apr 2026 09:00:00 +0900"
                }
              ]
            }
            """;

        server.expect(requestTo(Matchers.containsString("query=%EC%9D%B8%EA%B3%B5%EC%A7%80%EB%8A%A5")))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        NewsCollectionProperties properties = new NewsCollectionProperties(
            new NewsCollectionProperties.Naver(
                "https://openapi.naver.com/v1/search/news.json",
                "cid", "csec", 10),
            List.of()
        );
        NaverNewsClient client = new NaverNewsClient(builder.build(), properties);

        List<CollectedArticle> result = client.fetch("인공지능");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("인공지능 뉴스");
        server.verify();
    }

    @Test
    void fetch_HTTP_오류_예외_전파() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        server.expect(requestTo(Matchers.startsWith("https://openapi.naver.com/v1/search/news.json")))
            .andRespond(withServerError());

        NewsCollectionProperties properties = new NewsCollectionProperties(
            new NewsCollectionProperties.Naver(
                "https://openapi.naver.com/v1/search/news.json",
                "cid", "csec", 10),
            List.of()
        );
        NaverNewsClient client = new NaverNewsClient(builder.build(), properties);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> client.fetch("Spring"))
            .isInstanceOf(org.springframework.web.client.HttpServerErrorException.class);
    }
}
