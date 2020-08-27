package com.plusauth.android.jwt;

import com.plusauth.android.BaseException;

/**
 * Exception thrown during token validation
 */
public class InvalidTokenException extends BaseException {
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidTokenException(String message) {
        super(message);
    }
}
