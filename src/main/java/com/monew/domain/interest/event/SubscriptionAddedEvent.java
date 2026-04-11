package com.monew.domain.interest.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SubscriptionAddedEvent(
    UUID userId,
    UUID interestId,
    String interestName,
    List<String> interestKeywords,
    long subscriberCount,
    Instant createdAt
) {

}
