package com.plusauth.android.crypto;

/**
 * Interface that is used to add support for credentials to be encrypted.
 */
public interface Encryptor {

    /**
     * @param text string that will be encrypted
     * @return encrypted string
     */
    String encrypt(String text);

    /**
     * @param text encrypted text to be decrypted
     * @return decrypted string
     */
    String decrypt(String text);
}
