package com.plusauth.android.auth.exceptions;

/**
 * Exception thrown if there are no browser apps on system.
 */
public class NoAvailableBrowserException extends AuthenticationException {

    public NoAvailableBrowserException() {
        super("pa.no_available_browser", "System does not have any browser apps, cannot continue authentication process.");
    }
}
