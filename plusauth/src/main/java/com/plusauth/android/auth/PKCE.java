
package com.plusauth.android.auth;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.plusauth.android.api.Api;
import com.plusauth.android.auth.exceptions.AuthenticationException;
import com.plusauth.android.crypto.CryptoUtil;
import com.plusauth.android.model.Credentials;
import com.plusauth.android.util.AuthenticationCallback;


/**
 * Helper class for Proof Key for Code Exchange.
 */
public class PKCE {

    private final Api apiClient;
    private final String codeVerifier;
    private final String redirectUri;
    private final String codeChallenge;

    public PKCE(@NonNull Api apiClient, @NonNull String redirectUri) {
        this.apiClient = apiClient;
        this.redirectUri = redirectUri;
        this.codeVerifier = CryptoUtil.secureRandomString();
        this.codeChallenge = CryptoUtil.generateCodeChallenge(codeVerifier);
    }

    public String getCodeChallenge() {
        return codeChallenge;
    }

    /**
     * Checks if pkce can be used, pkce requires sha256 to be available.
     *
     * @return whether pkce is available
     */
    @VisibleForTesting
    public static boolean isAvailable() {
        try {
            byte[] input = CryptoUtil.getASCIIBytes("test");
            CryptoUtil.getSHA256(input);
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }

    /**
     * Calls Api.token with provided authorization code and generated code verifier.
     *
     * @param authorizationCode auth code received from login flow
     * @param callback callback for delivering results
     */
    public void getToken(String authorizationCode, @NonNull final AuthenticationCallback callback) {
        apiClient.token(authorizationCode, redirectUri)
                .setCodeVerifier(codeVerifier)
                .call(new AuthenticationCallback() {
                    @Override
                    public void onSuccess(Credentials payload) {
                        callback.onSuccess(payload);
                    }

                    @Override
                    public void onFailure(AuthenticationException error) {
                        callback.onFailure(error);
                    }
                });
    }
}
