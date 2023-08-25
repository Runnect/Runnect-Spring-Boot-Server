package org.runnect.server.common.exception;

public class BadRequestException extends BasicException {
    public BadRequestException(ErrorStatus errorStatus, String message) {
        super(errorStatus, message);
    }
}
