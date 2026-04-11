package com.monew.domain.article.mapper;

import com.monew.domain.article.dto.ArticleDto;
import com.monew.domain.article.entity.Article;
import com.monew.domain.interest.entity.Interest;
import org.springframework.stereotype.Component;

@Component
public class ArticleMapper {

    public ArticleDto toDto(Article article, boolean viewedByMe) {
        Interest interest = article.getInterest();
        return new ArticleDto(
            article.getId(),
            article.getSource(),
            article.getSourceUrl(),
            article.getTitle(),
            article.getSummary(),
            article.getPublishedAt(),
            interest != null ? interest.getId() : null,
            interest != null ? interest.getName() : null,
            article.getViewCount(),
            article.getCommentCount(),
            viewedByMe
        );
    }
}
