package org.runnect.server.common.exception;

import org.runnect.server.common.constant.ErrorStatus;

public class ConflictException extends BasicException {
    public ConflictException(ErrorStatus errorStatus, String message) {
        super(errorStatus, message);
    }
}
