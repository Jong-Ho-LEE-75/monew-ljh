package com.monew.domain.article.repository;

import com.monew.domain.article.entity.Article;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleRepository
    extends JpaRepository<Article, UUID>, JpaSpecificationExecutor<Article> {

    boolean existsBySourceUrl(String sourceUrl);

    @Query("""
        select a from Article a
        where a.createdAt >= :from and a.createdAt < :to
        order by a.createdAt asc
        """)
    List<Article> findAllCreatedBetween(
        @Param("from") Instant from,
        @Param("to") Instant to
    );
}
