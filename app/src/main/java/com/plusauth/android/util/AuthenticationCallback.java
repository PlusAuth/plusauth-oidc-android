package com.plusauth.android.util;

import com.plusauth.android.auth.exceptions.AuthenticationException;
import com.plusauth.android.model.Credentials;

/**
 * Convenience class for {@literal PACallback<Credentials, AuthenticationException>} types of callback.
 */
public interface AuthenticationCallback extends PACallback<Credentials, AuthenticationException> {
}
