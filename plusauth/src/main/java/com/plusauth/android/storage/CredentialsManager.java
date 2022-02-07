package com.plusauth.android.storage;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.google.gson.Gson;
import com.plusauth.android.api.Api;
import com.plusauth.android.api.internal.GsonFactory;
import com.plusauth.android.auth.exceptions.AuthenticationException;
import com.plusauth.android.crypto.Encryptor;
import com.plusauth.android.model.Credentials;
import com.plusauth.android.util.AuthenticationCallback;
import com.plusauth.android.util.PACallback;
import com.plusauth.android.util.PLog;

/**
 * Class that handles credentials and allows to save and retrieve them. Uses internal caching
 * for faster results on repeated calls.
 */
public class CredentialsManager {
    private static final String KEY_CREDENTIALS = "pa.credentials";

    private final Api authClient;
    private final Storage storage;
    private final Encryptor encryptor;
    private final Gson gson;
    private Credentials cachedCredentials;

    public CredentialsManager(@NonNull Api authenticationClient, @NonNull Storage storage, Encryptor encryptor) {
        this.authClient = authenticationClient;
        this.storage = storage;
        this.encryptor = encryptor;
        this.gson = GsonFactory.build();
    }

    /**
     * Serializes, encrypts and stores given credentials using provided Storage
     * and Encryptor objects.
     *
     * @param credentials credentials to be saved
     */
    public void saveCredentials(@NonNull Credentials credentials) {
        String credsJson = gson.toJson(credentials);
        storage.write(KEY_CREDENTIALS, encryptor.encrypt(credsJson));
        cachedCredentials = credentials;
    }

    /**
     * Checks to see if there are any valid credentials and returns them. If the credentials are
     * expired and they have a refresh token, they will be refreshed and returned. Otherwise callback
     * with an exception will be called.
     *
     * @param callback results of valid credentials or an exception
     */
    public void getCredentials(@NonNull final PACallback<Credentials, CredentialsManagerException> callback) {
        final Credentials credentials = getCredentialsWithoutValidation();

        if (credentials == null) {
            callback.onFailure(new CredentialsManagerException("No Credentials were previously set."));
            return;
        }

        if (!credentials.isExpired()) {
            callback.onSuccess(credentials);
            return;
        }

        if (credentials.getRefreshToken() == null) {
            callback.onFailure(new CredentialsManagerException("Credentials have expired and no Refresh Token was available to renew them."));
            return;
        }

        authClient.renewAuth(credentials.getRefreshToken()).call(new AuthenticationCallback() {
            @Override
            public void onSuccess(Credentials fresh) {
                //non-empty refresh token for refresh token rotation scenarios
                String updatedRefreshToken = TextUtils.isEmpty(fresh.getRefreshToken()) ? credentials.getRefreshToken() : fresh.getRefreshToken();
                Credentials finalCreds = new Credentials(fresh.getIdToken(), fresh.getAccessToken(), fresh.getType(), updatedRefreshToken, fresh.getExpiresAt().getTime(), fresh.getScope());
                saveCredentials(finalCreds);
                callback.onSuccess(finalCreds);
            }

            @Override
            public void onFailure(AuthenticationException error) {
                callback.onFailure(new CredentialsManagerException("An error occurred while trying to use the Refresh Token to renew the Credentials.", error));
            }
        });
    }


    /**
     * Synchronous version of {@literal getCredentials}.
     *
     * @return validated credentials
     */
    public Credentials getCredentialsSync() {
        final Credentials credentials = getCredentialsWithoutValidation();

        if (credentials == null) {
            throw new CredentialsManagerException("No Credentials were previously set.");
        }

        if (!credentials.isExpired()) {
            return credentials;
        }

        if (credentials.getRefreshToken() == null) {
            throw new CredentialsManagerException("Credentials have expired and no Refresh Token was available to renew them.");
        }

        try {
            Credentials fresh = authClient.renewAuth(credentials.getRefreshToken()).callSync();
            String updatedRefreshToken = TextUtils.isEmpty(fresh.getRefreshToken()) ? credentials.getRefreshToken() : fresh.getRefreshToken();
            Credentials finalCreds = new Credentials(fresh.getIdToken(), fresh.getAccessToken(), fresh.getType(), updatedRefreshToken, fresh.getExpiresAt().getTime(), fresh.getScope());
            saveCredentials(finalCreds);

            return finalCreds;
        } catch (AuthenticationException e) {
            throw new CredentialsManagerException("An error occurred while trying to use the Refresh Token to renew the Credentials.", e);
        }
    }

    /**
     * Tries to read/parse/decrypt credential from storage returns it without validation
     * or refresh. Null will be returned if there are any exceptions.
     *
     * @return stored credentials or null
     */
    @Nullable
    public Credentials getCredentialsWithoutValidation() {
        if (cachedCredentials != null) return cachedCredentials;

        try {
            String credsJsonEnc = storage.read(KEY_CREDENTIALS);
            String credsJson = encryptor.decrypt(credsJsonEnc);

            Credentials credentials = gson.fromJson(credsJson, Credentials.class);
            cachedCredentials = credentials;

            return credentials;
        } catch (Exception e) {
            PLog.e("An error occurred while getting credentials from storage.", e);
            return null;
        }
    }

    /**
     * Checks if there are credentials and whether they are non-expired or refreshable.
     *
     * @return whether credentials are valid
     */
    public boolean hasValidCredentials() {
        Credentials credentials = getCredentialsWithoutValidation();

        return credentials != null &&
                (!credentials.isExpired() || credentials.getRefreshToken() != null);
    }

    /**
     * Deletes the credentials from the storage.
     */
    public void clearCredentials() {
        storage.delete(KEY_CREDENTIALS);
        cachedCredentials = null;
    }

    @VisibleForTesting
    long getCurrentTimeInMillis() {
        return System.currentTimeMillis();
    }

}
