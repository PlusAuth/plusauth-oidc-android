
package com.plusauth.android.auth;

import com.plusauth.android.auth.exceptions.AuthenticationException;
import com.plusauth.android.util.AuthenticationCallback;

/**
 * Convenience class that implements onFailure.
 */
public abstract class SimpleAuthCallback implements AuthenticationCallback {

    private final AuthenticationCallback baseCallback;

    public SimpleAuthCallback(AuthenticationCallback baseCallback) {
        this.baseCallback = baseCallback;
    }


    public void onFailure(AuthenticationException exception) {
        baseCallback.onFailure(exception);
    }

}