package com.plusauth.android.crypto;

import android.util.Base64;

import androidx.annotation.NonNull;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Utility class that exposes simple crypto functions.
 */
public abstract class CryptoUtil {

    private static final String TAG = CryptoUtil.class.getSimpleName();

    /**
     * Provides random base64 encoded string generated from 32 random secure bytes.
     *
     * @return random string
     */
    public static String secureRandomString() {
        final byte[] randomBytes = generateSecureBytes(32);
        return encodeBase64(randomBytes);
    }

    /**
     * Provides random secure bytes of given length.
     *
     * @param length length of random bytes
     * @return random bytes
     */
    public static byte[] generateSecureBytes(int length) {
        final SecureRandom sr = new SecureRandom();
        final byte[] randomBytes = new byte[length];
        sr.nextBytes(randomBytes);
        return randomBytes;
    }

    /**
     * Base64 encodes provided source byte array.
     *
     * @param source byte array to be encoded
     * @return encoded result
     */
    public static String encodeBase64(byte[] source) {
        return Base64.encodeToString(source, 8 | 2 | 1);
    }

    /**
     * Base64 decodes provided source string.
     *
     * @param source string to be decoded
     * @return decoded result
     */
    public static byte[] decodeBase64(String source) {
        return Base64.decode(source, 8 | 2 | 1);
    }

    /**
     * Base64 decodes provided source string and converts it to string.
     *
     * @param source string to be decoded
     * @return decoded result
     */
    public static String decodeBase64ToString(String source) {
        return new String(decodeBase64(source), Charset.defaultCharset());
    }

    /**
     * Provides ASCII bytes of input string.
     *
     * @param value string to be converted
     * @return ascii byte array
     */
    public static byte[] getASCIIBytes(String value) {
        return value.getBytes(Charset.forName("US-ASCII"));
    }

    /**
     * Provides SHA256 hash of given input.
     *
     * @param input byte to be hashed
     * @return hash
     */
    public static byte[] getSHA256(byte[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(input, 0, input.length);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to get SHA-256 signature", e);
        }
    }

    /**
     * Generates code challenge by hashing and encoding given input string.
     *
     * @param codeVerifier string for derivation
     * @return code challenge
     */
    public static String generateCodeChallenge(@NonNull String codeVerifier) {
        byte[] input = getASCIIBytes(codeVerifier);
        byte[] signature = getSHA256(input);
        return encodeBase64(signature);
    }
}
