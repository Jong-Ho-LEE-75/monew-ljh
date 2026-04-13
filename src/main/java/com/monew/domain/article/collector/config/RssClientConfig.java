package com.monew.domain.article.collector.config;

import com.monew.domain.article.collector.NewsSourceClient;
import com.monew.domain.article.collector.client.RssNewsClient;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Slf4j
@Configuration
public class RssClientConfig {

    @Bean
    List<RssNewsClient> rssNewsClients(
        NewsCollectionProperties properties,
        RestClient newsRestClient
    ) {
        List<NewsCollectionProperties.RssFeed> feeds = properties.rss();
        if (feeds == null || feeds.isEmpty()) {
            return List.of();
        }
        List<RssNewsClient> clients = feeds.stream()
            .map(feed -> new RssNewsClient(feed.source(), feed.url(), newsRestClient))
            .toList();
        log.info("RSS 클라이언트 등록 완료: {}",
            clients.stream().map(RssNewsClient::sourceName).toList());
        return clients;
    }
}
