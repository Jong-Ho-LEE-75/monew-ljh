package com.monew.domain.interest.exception;

import com.monew.common.exception.ErrorCode;
import java.util.Map;

public class DuplicateInterestNameException extends InterestException {

    public DuplicateInterestNameException(String requested, String conflict, double similarity) {
        super(
            ErrorCode.DUPLICATE_INTEREST_NAME,
            Map.of(
                "requested", requested,
                "conflict", conflict,
                "similarity", similarity
            )
        );
    }
}
