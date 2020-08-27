package com.plusauth.android.crypto;

import com.plusauth.android.BaseException;

/**
 * Exception that is thrown for EncryptionRelated exceptions.
 */
public class EncryptionException extends BaseException {
    public EncryptionException(String message, Throwable cause) {
        super(message, cause);
    }

    public EncryptionException(String message) {
        super(message);
    }
}
