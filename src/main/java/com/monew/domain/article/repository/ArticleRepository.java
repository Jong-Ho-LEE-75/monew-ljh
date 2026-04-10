package com.monew.domain.article.repository;

import com.monew.domain.article.entity.Article;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, UUID> {

    boolean existsBySourceUrl(String sourceUrl);
}
