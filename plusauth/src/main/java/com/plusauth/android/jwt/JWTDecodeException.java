package com.plusauth.android.jwt;

/**
 * Exception thrown when JWT decoding fails
 */
public class JWTDecodeException extends RuntimeException {

    public JWTDecodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public JWTDecodeException(String message) {
        super(message);
    }
}
