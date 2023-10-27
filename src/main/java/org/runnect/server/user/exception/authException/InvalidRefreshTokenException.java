package org.runnect.server.user.exception.authException;

import lombok.Getter;
import org.runnect.server.common.constant.ErrorStatus;
import org.runnect.server.common.exception.BasicException;

@Getter
public class InvalidRefreshTokenException extends BasicException{
    public InvalidRefreshTokenException(ErrorStatus errorStatus, String message){
        super(errorStatus,message);
    }
}

