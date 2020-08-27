package com.plusauth.android;

/**
 * Base Runtime Exception for the library.
 */
public class BaseException extends RuntimeException{
    public BaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseException(String message) {
        super(message);
    }
}
