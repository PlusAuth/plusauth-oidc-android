package com.plusauth.android.storage;

import com.plusauth.android.BaseException;
import com.plusauth.android.auth.exceptions.AuthenticationException;

/**
 * Exception thrown when calling CredentialManager methods. Most likely case will be invalid credentials.
 */
public class CredentialsManagerException extends BaseException {
    public CredentialsManagerException(String s, AuthenticationException error) {
        super(s, error);
    }
    public CredentialsManagerException(String s) {
        super(s);
    }
}
