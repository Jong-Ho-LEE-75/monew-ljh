package com.monew.domain.user.exception;

import com.monew.common.exception.ErrorCode;
import java.util.Map;

public class DuplicateUserException extends UserException {

    public DuplicateUserException(String email) {
        super(ErrorCode.DUPLICATE_USER, Map.of("email", email));
    }
}
