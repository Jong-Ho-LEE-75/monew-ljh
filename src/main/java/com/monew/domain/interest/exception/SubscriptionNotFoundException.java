package com.monew.domain.interest.exception;

import com.monew.common.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class SubscriptionNotFoundException extends InterestException {

    public SubscriptionNotFoundException(UUID userId, UUID interestId) {
        super(
            ErrorCode.SUBSCRIPTION_NOT_FOUND,
            Map.of("userId", userId, "interestId", interestId)
        );
    }
}
