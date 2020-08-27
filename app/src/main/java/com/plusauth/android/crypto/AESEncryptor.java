package com.plusauth.android.crypto;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.plusauth.android.util.PLog;

import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Provides military grade 256 bit AES-GCM encryption. Provide instance of this class
 * to PlusAuthBuilder for credential encryption. There are edge cases that may invalidate your
 * AES keys which would clear users local session and log them out. We recommend not to use
 * added encryption unless there is a hard requirement for it.
 * To learn more check out this article:
 * @see <a href="https://developer.android.com/training/articles/keystore">KeyStore Docs</a>
 */
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class AESEncryptor implements Encryptor {
    private static final int IV_LENGTH = 128;
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private final String algorithm = "AES/GCM/NoPadding";
    private Key key;
    private String KEY_ALIAS = "pa.aes.key";

    /**
     * AndroidKeyStore backed KeyGenerator is supported only after API 23 hence it is very difficult to
     * store the AES encryption key securely. This constructor leaves the key storing responsibility to
     * you but allows you to utilize this class.
     *
     * @param key the key that will be used in encrypt/decrypt operations
     */
    public AESEncryptor(@NonNull SecretKey key) {
        this.key = key;
    }

    /**
     * Uses AndroidKeyStore to store the AES key. If the key does not exist then it will
     * generate one for you.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public AESEncryptor() {
        this.key = getKey();
    }

    @Override
    public String encrypt(String text) {
        PLog.d("Using AESEncryptor to encrypt credentials.");
        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] ciphertext = cipher.doFinal(text.getBytes("UTF-8"));

            return CryptoUtil.encodeBase64(cipher.getIV()) + "#" + CryptoUtil.encodeBase64(ciphertext);
        } catch (Exception e) {
            throw new EncryptionException("AES Encryption failed", e);
        }
    }

    @Override
    public String decrypt(String text) {
        PLog.d("Using AESEncryptor to decrypt credentials.");
        try {
            String[] split = text.split("#");
            byte[] iv = CryptoUtil.decodeBase64(split[0]);
            byte[] encryptedText = CryptoUtil.decodeBase64(split[1]);


            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(IV_LENGTH, iv));
            byte[] decryptedText = cipher.doFinal(encryptedText);

            return new String(decryptedText);
        } catch (Exception e) {
            throw new EncryptionException("AES Decryption failed", e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private Key getKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);

            if (!keyStore.containsAlias(KEY_ALIAS)) {
                return generateKey();
            }

            return keyStore.getKey(KEY_ALIAS, null);

        } catch (Exception e) {
            throw new EncryptionException("AES failed to retrieve/create keys from KeyStore.", e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected Key generateKey() throws InvalidAlgorithmParameterException, NoSuchProviderException, NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
        keyGenerator.init(
                new KeyGenParameterSpec.Builder(KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .build());
        return keyGenerator.generateKey();
    }
}
