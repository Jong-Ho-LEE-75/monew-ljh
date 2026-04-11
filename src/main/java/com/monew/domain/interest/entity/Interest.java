package com.monew.domain.interest.entity;

import com.monew.common.entity.BaseUpdatableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "interests")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Interest extends BaseUpdatableEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false)
    private long subscriberCount = 0;

    @OneToMany(mappedBy = "interest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InterestKeyword> keywords = new ArrayList<>();

    @Builder
    private Interest(String name, List<String> keywords) {
        this.name = name;
        this.subscriberCount = 0;
        if (keywords != null) {
            keywords.forEach(this::addKeyword);
        }
    }

    public void addKeyword(String keyword) {
        InterestKeyword k = InterestKeyword.of(this, keyword);
        this.keywords.add(k);
    }

    public void replaceKeywords(List<String> newKeywords) {
        this.keywords.clear();
        if (newKeywords != null) {
            newKeywords.forEach(this::addKeyword);
        }
    }

    public void increaseSubscriber() {
        this.subscriberCount++;
    }

    public void decreaseSubscriber() {
        if (this.subscriberCount > 0) {
            this.subscriberCount--;
        }
    }
}
