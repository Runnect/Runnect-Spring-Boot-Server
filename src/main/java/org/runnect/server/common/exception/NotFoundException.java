package org.runnect.server.common.exception;

public class NotFoundException extends BasicException {
    public NotFoundException(ErrorStatus errorStatus, String message) {
        super(errorStatus, message);
    }
}
