package com.monew.domain.activity.exception;

import com.monew.common.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class UserActivityNotFoundException extends ActivityException {

    public UserActivityNotFoundException(UUID userId) {
        super(ErrorCode.ACTIVITY_NOT_FOUND, Map.of("userId", userId));
    }
}
