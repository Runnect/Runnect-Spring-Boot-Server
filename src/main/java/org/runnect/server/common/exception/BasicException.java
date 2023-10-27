package org.runnect.server.common.exception;

import lombok.Getter;
import org.runnect.server.common.constant.ErrorStatus;

@Getter
public class BasicException extends RuntimeException {
    private final ErrorStatus errorStatus;

    public BasicException(ErrorStatus errorStatus, String message) {
        super(message);
        this.errorStatus = errorStatus;
    }

    public int getHttpStatus() {
        return errorStatus.getHttpStatusCode();
    }

}
