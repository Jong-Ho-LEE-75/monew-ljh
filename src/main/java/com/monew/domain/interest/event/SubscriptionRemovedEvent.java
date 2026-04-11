package com.monew.domain.interest.event;

import java.util.UUID;

public record SubscriptionRemovedEvent(
    UUID userId,
    UUID interestId
) {

}
