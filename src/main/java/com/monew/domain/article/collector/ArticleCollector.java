package com.monew.domain.article.collector;

import com.monew.common.metrics.MonewMetrics;
import com.monew.domain.article.entity.Article;
import com.monew.domain.article.event.ArticleCollectedEvent;
import com.monew.domain.article.repository.ArticleRepository;
import com.monew.domain.article.collector.client.RssNewsClient;
import com.monew.domain.interest.entity.Interest;
import com.monew.domain.interest.entity.InterestKeyword;
import com.monew.domain.interest.repository.InterestRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ArticleCollector {

    private final List<NewsSourceClient> clients;
    private final InterestRepository interestRepository;
    private final ArticleRepository articleRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final MonewMetrics metrics;

    public ArticleCollector(
        List<NewsSourceClient> clients,
        List<RssNewsClient> rssNewsClients,
        InterestRepository interestRepository,
        ArticleRepository articleRepository,
        ApplicationEventPublisher eventPublisher,
        MonewMetrics metrics
    ) {
        List<NewsSourceClient> allClients = new ArrayList<>(clients);
        allClients.addAll(rssNewsClients);
        this.clients = allClients;
        this.interestRepository = interestRepository;
        this.articleRepository = articleRepository;
        this.eventPublisher = eventPublisher;
        this.metrics = metrics;
    }

    @Transactional
    public CollectionResult collect() {
        try {
            return metrics.collectionTimer().recordCallable((Callable<CollectionResult>) this::doCollect);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CollectionResult doCollect() {
        List<Interest> interests = interestRepository.findAll();
        int attempted = 0;
        int saved = 0;
        int duplicated = 0;

        for (Interest interest : interests) {
            List<String> keywords = interest.getKeywords().stream()
                .map(InterestKeyword::getKeyword)
                .toList();
            if (keywords.isEmpty()) {
                continue;
            }

            for (NewsSourceClient client : clients) {
                List<CollectedArticle> fetched;
                try {
                    fetched = client.fetch(interest.getName());
                } catch (Exception e) {
                    log.warn("수집 실패 source={} interest={}", client.sourceName(), interest.getName(), e);
                    metrics.incrementCollectionFailure();
                    continue;
                }

                for (CollectedArticle candidate : fetched) {
                    attempted++;
                    if (!KeywordMatcher.matches(candidate, keywords)) {
                        continue;
                    }
                    if (articleRepository.existsBySourceUrl(candidate.sourceUrl())) {
                        duplicated++;
                        continue;
                    }
                    try {
                        Article savedArticle = articleRepository.save(toEntity(candidate, interest));
                        saved++;
                        eventPublisher.publishEvent(new ArticleCollectedEvent(
                            savedArticle.getId(),
                            interest.getId(),
                            interest.getName(),
                            savedArticle.getTitle()
                        ));
                    } catch (DataIntegrityViolationException e) {
                        duplicated++;
                    }
                }
            }
        }

        log.info("수집 완료 attempted={} saved={} duplicated={}", attempted, saved, duplicated);
        metrics.incrementArticlesSaved(saved);
        metrics.incrementArticlesDuplicated(duplicated);
        return new CollectionResult(attempted, saved, duplicated);
    }

    private Article toEntity(CollectedArticle candidate, Interest interest) {
        return Article.builder()
            .source(candidate.source())
            .sourceUrl(candidate.sourceUrl())
            .title(candidate.title())
            .summary(candidate.summary())
            .publishedAt(candidate.publishedAt())
            .interest(interest)
            .build();
    }

    public record CollectionResult(int attempted, int saved, int duplicated) {

    }
}
