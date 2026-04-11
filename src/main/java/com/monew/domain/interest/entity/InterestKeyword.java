package com.monew.domain.interest.entity;

import com.monew.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "interest_keywords")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterestKeyword extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_id", nullable = false)
    private Interest interest;

    @Column(nullable = false, length = 100)
    private String keyword;

    private InterestKeyword(Interest interest, String keyword) {
        this.interest = interest;
        this.keyword = keyword;
    }

    static InterestKeyword of(Interest interest, String keyword) {
        return new InterestKeyword(interest, keyword);
    }
}
