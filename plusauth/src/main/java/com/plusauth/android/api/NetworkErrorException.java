package com.plusauth.android.api;

import com.plusauth.android.BaseException;

/**
 * Exception for network related errors
 */
public class NetworkErrorException extends BaseException {

    public NetworkErrorException(Throwable cause) {
        super("Failed to send/receive the network request", cause);
    }
}
