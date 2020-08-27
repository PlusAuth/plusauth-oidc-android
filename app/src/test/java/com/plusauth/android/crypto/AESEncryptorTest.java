package com.plusauth.android.crypto;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.assertEquals;


class AESEncryptorTest {

    private static SecretKey key;
    private static AESEncryptor aesEncryptor;

    @BeforeAll
    static void setUp() throws NoSuchAlgorithmException {
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        keygen.init(256);
        key = keygen.generateKey();
        aesEncryptor = new AESEncryptor(key);
    }

    @Test
    void shouldEncryptAndDecrypt() {
        String clearTest = "test";
        String encryptedText = aesEncryptor.encrypt(clearTest);
        String decryptedText = aesEncryptor.decrypt(encryptedText);

        assertEquals(clearTest, decryptedText);
    }
}