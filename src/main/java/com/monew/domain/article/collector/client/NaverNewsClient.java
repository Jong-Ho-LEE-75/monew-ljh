package com.monew.domain.article.collector.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.monew.domain.article.collector.CollectedArticle;
import com.monew.domain.article.collector.NewsSourceClient;
import com.monew.domain.article.collector.config.NewsCollectionProperties;
import java.net.URI;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverNewsClient implements NewsSourceClient {

    private static final String SOURCE_NAME = "NAVER";
    private static final String HEADER_CLIENT_ID = "X-Naver-Client-Id";
    private static final String HEADER_CLIENT_SECRET = "X-Naver-Client-Secret";
    private static final DateTimeFormatter PUB_DATE_FORMAT = DateTimeFormatter.RFC_1123_DATE_TIME;

    private final RestClient newsRestClient;
    private final NewsCollectionProperties properties;

    @Override
    public String sourceName() {
        return SOURCE_NAME;
    }

    @Override
    public List<CollectedArticle> fetch(String query) {
        NewsCollectionProperties.Naver naver = properties.naver();
        if (naver == null || !naver.isEnabled()) {
            log.debug("Naver 뉴스 수집 비활성화 (API 키 없음)");
            return List.of();
        }

        URI uri = UriComponentsBuilder.fromUriString(naver.baseUrl())
            .queryParam("query", query)
            .queryParam("display", naver.displayOrDefault())
            .queryParam("sort", "date")
            .encode()
            .build()
            .toUri();

        NaverSearchResponse response = newsRestClient.get()
            .uri(uri)
            .header(HEADER_CLIENT_ID, naver.clientId())
            .header(HEADER_CLIENT_SECRET, naver.clientSecret())
            .header(HttpHeaders.ACCEPT, "application/json")
            .retrieve()
            .body(NaverSearchResponse.class);

        if (response == null || response.items() == null) {
            return List.of();
        }

        return response.items().stream()
            .map(this::toCollected)
            .toList();
    }

    private CollectedArticle toCollected(NaverSearchItem item) {
        return new CollectedArticle(
            SOURCE_NAME,
            item.originallink() != null && !item.originallink().isBlank() ? item.originallink() : item.link(),
            stripTags(item.title()),
            stripTags(item.description()),
            parsePubDate(item.pubDate())
        );
    }

    private static String stripTags(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("<[^>]*>", "")
            .replace("&quot;", "\"")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .trim();
    }

    private static Instant parsePubDate(String pubDate) {
        if (pubDate == null || pubDate.isBlank()) {
            return Instant.now();
        }
        try {
            return ZonedDateTime.parse(pubDate, PUB_DATE_FORMAT).toInstant();
        } catch (Exception e) {
            log.debug("Naver pubDate 파싱 실패: {}", pubDate);
            return Instant.now();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NaverSearchResponse(List<NaverSearchItem> items) {

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NaverSearchItem(
        String title,
        String originallink,
        String link,
        String description,
        String pubDate
    ) {

    }
}
