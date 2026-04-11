package com.monew.domain.article.backup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monew.common.metrics.MonewMetrics;
import com.monew.domain.article.entity.Article;
import com.monew.domain.article.repository.ArticleRepository;
import com.monew.domain.interest.entity.Interest;
import com.monew.domain.interest.repository.InterestRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleBackupService {

    private final ArticleRepository articleRepository;
    private final InterestRepository interestRepository;
    private final ArticleBackupStorage storage;
    private final ObjectMapper objectMapper;
    private final MonewMetrics metrics;

    @Transactional(readOnly = true)
    public int backup(LocalDate date) {
        Instant from = date.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        List<ArticleBackupRecord> records = articleRepository.findAllCreatedBetween(from, to).stream()
            .map(this::toRecord)
            .toList();

        String json;
        try {
            json = objectMapper.writeValueAsString(records);
        } catch (JsonProcessingException e) {
            metrics.incrementBackupFailure();
            throw new IllegalStateException("백업 직렬화 실패 date=" + date, e);
        }

        try {
            storage.upload(date, json);
        } catch (RuntimeException e) {
            metrics.incrementBackupFailure();
            throw e;
        }
        metrics.incrementBackupUploaded();
        log.info("백업 업로드 완료 date={} count={}", date, records.size());
        return records.size();
    }

    @Transactional
    public int restore(LocalDate date) {
        Optional<String> payload = storage.download(date);
        if (payload.isEmpty()) {
            log.warn("복구할 백업이 없습니다 date={}", date);
            return 0;
        }

        List<ArticleBackupRecord> records;
        try {
            records = objectMapper.readValue(payload.get(), new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            metrics.incrementBackupFailure();
            throw new IllegalStateException("백업 역직렬화 실패 date=" + date, e);
        }

        int restored = 0;
        for (ArticleBackupRecord record : records) {
            if (articleRepository.existsBySourceUrl(record.sourceUrl())) {
                continue;
            }
            Interest interest = record.interestId() == null ? null
                : interestRepository.findById(record.interestId()).orElse(null);
            articleRepository.save(Article.builder()
                .source(record.source())
                .sourceUrl(record.sourceUrl())
                .title(record.title())
                .summary(record.summary())
                .publishedAt(record.publishedAt())
                .interest(interest)
                .build());
            restored++;
        }

        log.info("백업 복구 완료 date={} restored={} skipped={}",
            date, restored, records.size() - restored);
        metrics.incrementBackupRestored(restored);
        return restored;
    }

    public List<LocalDate> listBackupDates() {
        return storage.listDates();
    }

    private ArticleBackupRecord toRecord(Article article) {
        return new ArticleBackupRecord(
            article.getId(),
            article.getSource(),
            article.getSourceUrl(),
            article.getTitle(),
            article.getSummary(),
            article.getPublishedAt(),
            article.getInterest() == null ? null : article.getInterest().getId(),
            article.getCreatedAt()
        );
    }
}
