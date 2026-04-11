package com.monew.domain.article.backup;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ArticleBackupStorage {

    void upload(LocalDate date, String content);

    Optional<String> download(LocalDate date);

    List<LocalDate> listDates();
}
