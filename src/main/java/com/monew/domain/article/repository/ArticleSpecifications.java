package com.monew.domain.article.repository;

import com.monew.domain.article.dto.ArticleSearchCondition;
import com.monew.domain.article.dto.SortDirection;
import com.monew.domain.article.entity.Article;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class ArticleSpecifications {

    private ArticleSpecifications() {
    }

    public static Specification<Article> build(ArticleSearchCondition condition, Instant cursor) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));

            if (condition.interestId() != null) {
                predicates.add(cb.equal(root.get("interest").get("id"), condition.interestId()));
            }

            String keyword = condition.keyword();
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                Predicate titleLike = cb.like(cb.lower(root.get("title")), pattern);
                Predicate summaryLike = cb.like(cb.lower(root.get("summary")), pattern);
                predicates.add(cb.or(titleLike, summaryLike));
            }

            if (condition.sourceIn() != null && !condition.sourceIn().isEmpty()) {
                predicates.add(root.get("source").in(condition.sourceIn()));
            }

            if (condition.publishedFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("publishedAt"), condition.publishedFrom()));
            }

            if (condition.publishedTo() != null) {
                predicates.add(cb.lessThan(root.get("publishedAt"), condition.publishedTo()));
            }

            if (cursor != null) {
                if (condition.directionOrDefault() == SortDirection.DESC) {
                    predicates.add(cb.lessThan(root.get("publishedAt"), cursor));
                } else {
                    predicates.add(cb.greaterThan(root.get("publishedAt"), cursor));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
