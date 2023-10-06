package org.runnect.server.common.exception;

public class UnauthorizedException extends BasicException {

    public UnauthorizedException(ErrorStatus errorStatus, String message) {
        super(errorStatus, message);
    }
}
