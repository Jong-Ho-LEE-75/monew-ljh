package com.monew.domain.interest.exception;

import com.monew.common.exception.ErrorCode;
import com.monew.common.exception.MonewException;
import java.util.Map;

public class InterestException extends MonewException {

    public InterestException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
