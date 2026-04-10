package com.monew.domain.article.entity;

import com.monew.common.entity.BaseUpdatableEntity;
import com.monew.domain.interest.entity.Interest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "articles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article extends BaseUpdatableEntity {

    @Column(nullable = false, length = 50)
    private String source;

    @Column(nullable = false, unique = true, length = 1000)
    private String sourceUrl;

    @Column(nullable = false, length = 500)
    private String title;

    @Lob
    private String summary;

    @Column(nullable = false)
    private Instant publishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_id")
    private Interest interest;

    @Column(nullable = false)
    private long viewCount = 0;

    @Column(nullable = false)
    private boolean deleted = false;
}
