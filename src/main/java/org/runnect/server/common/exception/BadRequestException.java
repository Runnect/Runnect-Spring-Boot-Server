package org.runnect.server.common.exception;

import org.runnect.server.common.constant.ErrorStatus;

public class BadRequestException extends BasicException {
    public BadRequestException(ErrorStatus errorStatus, String message) {
        super(errorStatus, message);
    }
}
