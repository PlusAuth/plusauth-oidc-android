package com.plusauth.android.api;

import com.plusauth.android.BaseException;

/**
 * Exception that wraps errors that could happen when creating/encoding request body
 */
public class RequestBodyBuildException extends BaseException {

    public RequestBodyBuildException(String message, Throwable cause) {
        super(message, cause);
    }

}
