package org.runnect.server.common.exception;

import org.runnect.server.common.constant.ErrorStatus;

public class UnauthorizedException extends BasicException {

    public UnauthorizedException(ErrorStatus errorStatus, String message) {
        super(errorStatus, message);
    }
}
