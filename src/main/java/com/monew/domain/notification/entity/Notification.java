package com.monew.domain.notification.entity;

import com.monew.common.entity.BaseUpdatableEntity;
import com.monew.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "notifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseUpdatableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 500)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 30)
    private ResourceType resourceType;

    @Column(name = "resource_id", nullable = false)
    private UUID resourceId;

    @Column(nullable = false)
    private boolean confirmed;

    @Builder
    private Notification(User user, String content, ResourceType resourceType, UUID resourceId) {
        this.user = user;
        this.content = content;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.confirmed = false;
    }

    public void confirm() {
        this.confirmed = true;
    }

    public enum ResourceType {
        INTEREST,
        COMMENT
    }
}
