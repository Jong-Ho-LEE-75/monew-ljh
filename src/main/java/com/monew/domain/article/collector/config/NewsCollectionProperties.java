package com.monew.domain.article.collector.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "monew.news")
public record NewsCollectionProperties(
    Naver naver,
    List<RssFeed> rss
) {

    public NewsCollectionProperties {
        if (rss == null) {
            rss = List.of();
        }
    }

    public record Naver(
        String baseUrl,
        String clientId,
        String clientSecret,
        int display
    ) {

        public boolean isEnabled() {
            return clientId != null && !clientId.isBlank()
                && clientSecret != null && !clientSecret.isBlank();
        }

        public int displayOrDefault() {
            return display > 0 ? display : 20;
        }
    }

    public record RssFeed(
        String source,
        String url
    ) {

    }
}
