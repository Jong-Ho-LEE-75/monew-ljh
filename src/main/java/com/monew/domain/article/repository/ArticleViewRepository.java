package com.monew.domain.article.repository;

import com.monew.domain.article.entity.ArticleView;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleViewRepository extends JpaRepository<ArticleView, UUID> {

    boolean existsByArticle_IdAndUser_Id(UUID articleId, UUID userId);
}
