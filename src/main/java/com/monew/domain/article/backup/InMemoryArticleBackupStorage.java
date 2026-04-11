package com.monew.domain.article.backup;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "monew.backup", name = "type", havingValue = "memory", matchIfMissing = true)
public class InMemoryArticleBackupStorage implements ArticleBackupStorage {

    private final Map<LocalDate, String> store = new HashMap<>();

    @Override
    public void upload(LocalDate date, String content) {
        store.put(date, content);
    }

    @Override
    public Optional<String> download(LocalDate date) {
        return Optional.ofNullable(store.get(date));
    }

    @Override
    public List<LocalDate> listDates() {
        List<LocalDate> sorted = new ArrayList<>(store.keySet());
        sorted.sort(Comparator.reverseOrder());
        return sorted;
    }
}
