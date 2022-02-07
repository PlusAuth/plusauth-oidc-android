package com.plusauth.android.util;

import com.plusauth.android.auth.exceptions.AuthenticationException;

/**
 * Convenience class for {@literal PACallback<Void, AuthenticationException>} types of callback.
 */
public interface VoidCallback extends PACallback<Void, AuthenticationException> {
}