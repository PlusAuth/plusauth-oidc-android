package com.plusauth.android.crypto;

/**
 * Default Encryptor that just returns inputs back without any encryption.
 */
public class VoidEncryptor implements Encryptor {
    @Override
    public String encrypt(String text) {
        return text;
    }

    @Override
    public String decrypt(String text) {
        return text;
    }
}
