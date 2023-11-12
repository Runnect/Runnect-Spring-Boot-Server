package org.runnect.server.common.constant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenStatus {
    public static final long TOKEN_EXPIRED = -99;
    public static final long TOKEN_INVALID = -98;
    public static final long TOKEN_VALID = -97;
}


