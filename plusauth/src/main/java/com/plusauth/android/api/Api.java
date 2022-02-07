package com.plusauth.android.api;

import androidx.annotation.NonNull;

import com.plusauth.android.BaseException;
import com.plusauth.android.api.request.PAHttpClient;
import com.plusauth.android.api.request.ParameterizableRequest;
import com.plusauth.android.auth.exceptions.AuthenticationException;
import com.plusauth.android.model.Account;
import com.plusauth.android.model.Credentials;
import com.plusauth.android.model.UserProfile;
import com.plusauth.android.storage.CredentialsManager;

import java.util.Map;

import okhttp3.HttpUrl;


/**
 * Api interface for OIDC calls to your OIDC provider. Calls from this class does not alter local Storage
 * so if you want results to persist you must manually handle the Storage.
 */
public class Api {
    public static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
    public static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";

    private static final String OAUTH_CODE_KEY = "code";
    private static final String REDIRECT_URI_KEY = "redirect_uri";
    private static final String TOKEN_KEY = "token";
    private static final String OAUTH_PATH = "oauth";
    private static final String TOKEN_PATH = "token";
    private static final String USER_INFO_PATH = "me";
    private static final String REVOKE_PATH = "revocation";
    private static final String HEADER_AUTHORIZATION = "Authorization";

    private final Account account;
    private final PAHttpClient httpClient;
    private CredentialsManager credentialsManager;

    public Api(Account account, PAHttpClient httpClient) {
        this.account = account;
        this.httpClient = httpClient;
    }

    /**
     * CredentialManager to be used for no arg overload methods. If null, these methods will throw an exception.
     *
     * @param credentialsManager credential manager
     */
    public void setCredentialsManager(CredentialsManager credentialsManager) {
        this.credentialsManager = credentialsManager;
    }

    public String getClientId() {
        return account.getClientId();
    }

    /**
     * Calls the user info point of your oidc provider to retrieve profile of the access token's user.
     * Make sure that your accessToken has appropriate scopes such as profile, email etc. or you will get
     * an empty profile with null values.
     *
     * @return request call
     */
    public ParameterizableRequest<UserProfile, AuthenticationException> userInfo() {
        try {
            String accessToken = credentialsManager.getCredentialsWithoutValidation().getAccessToken();
            return userInfo(accessToken);
        } catch (NullPointerException e) {
            throw new BaseException("Failed to get access token from credentials manager. Make sure you have credentials with refresh token previously stored.", e);
        }
    }

    /**
     * Calls the user info point of your oidc provider to retrieve profile of the access token's user.
     * Make sure that your accessToken has appropriate scopes such as profile, email etc. or you will get
     * an empty profile with null values.
     *
     * @param accessToken will be used to get user info
     * @return request call
     */
    public ParameterizableRequest<UserProfile, AuthenticationException> userInfo(@NonNull String accessToken) {
        HttpUrl url = account.getDomainUrl().newBuilder()
                .addPathSegment(USER_INFO_PATH)
                .build();

        ParameterizableRequest<UserProfile, AuthenticationException> req = httpClient.GET(url, UserProfile.class);

        return req
                .addHeader(HEADER_AUTHORIZATION, "Bearer " + accessToken);
    }

    /**
     * Revoke a access/refresh token by calling revoke endpoint of your oidc provider. Revoked tokens
     * cannot be used again.
     *
     * @param token access/refresh token to be revoked
     * @return request call
     */
    public ParameterizableRequest<Void, AuthenticationException> revokeToken(@NonNull String token) {
        final Map<String, Object> parameters = MapBuilder.newBuilder()
                .setClientId(getClientId())
                .set(TOKEN_KEY, token)
                .build();

        HttpUrl url = account.getDomainUrl().newBuilder()
                .addPathSegment(TOKEN_PATH)
                .addPathSegment(REVOKE_PATH)
                .build();

        return httpClient.POST(url)
                .addParameters(parameters)
                .useUrlencodedBody();
    }

    /**
     * Revoke a access/refresh token by calling revoke endpoint of your oidc provider. Revoked tokens
     * cannot be used again.
     *
     * @return request call
     */
    public ParameterizableRequest<Void, AuthenticationException> revokeToken() {
        try {
            String refreshToken = credentialsManager.getCredentialsWithoutValidation().getRefreshToken();
            return revokeToken(refreshToken);
        } catch (NullPointerException e) {
            throw new BaseException("Failed to get refresh token from credentials manager. Make sure you have credentials with refresh token previously stored.", e);
        }
    }

    /**
     * Returns new set of credentials with renewed expiration dates.
     *
     * @return request call
     */
    public ParameterizableRequest<Credentials, AuthenticationException> renewAuth() {
        try {
            String refreshToken = credentialsManager.getCredentialsWithoutValidation().getRefreshToken();
            return renewAuth(refreshToken);
        } catch (NullPointerException e) {
            throw new BaseException("Failed to get refresh token from credentials manager. Make sure you have credentials with refresh token previously stored.", e);
        }
    }

    /**
     * Returns new set of credentials with renewed expiration dates.
     *
     * @param refreshToken token that will be used to renew
     * @return request call
     */
    public ParameterizableRequest<Credentials, AuthenticationException> renewAuth(@NonNull String refreshToken) {
        final Map<String, Object> parameters = MapBuilder.newBuilder()
                .setClientId(getClientId())
                .setRefreshToken(refreshToken)
                .setGrantType(GRANT_TYPE_REFRESH_TOKEN)
                .build();

        HttpUrl url = account.getDomainUrl().newBuilder()
                .addPathSegment(OAUTH_PATH)
                .addPathSegment(TOKEN_PATH)
                .build();


        return httpClient.POST(url, Credentials.class)
                .addParameters(parameters)
                .useUrlencodedBody();
    }


    /**
     * Exchange authorization code for credentials. You must set the code verifier with .setCodeVerifier
     * method if you are not using secret keys(which should not be used with public android apps).
     *
     * @param authorizationCode received from authorization flow
     * @return request call
     */
    public TokenRequest token(@NonNull String authorizationCode) {
        return token(authorizationCode, account.getRedirectUri());
    }

    /**
     * Exchange authorization code for credentials. You must set the code verifier with .setCodeVerifier
     * method if you are not using secret keys(which should not be used with public android apps).
     *
     * @param authorizationCode received from authorization flow
     * @param redirectUri uri that will be called to deliver response {applicationId}/callback by default
     * @return request call
     */
    public TokenRequest token(@NonNull String authorizationCode, @NonNull String redirectUri) {
        Map<String, Object> parameters = MapBuilder.newBuilder()
                .setClientId(getClientId())
                .setGrantType(GRANT_TYPE_AUTHORIZATION_CODE)
                .set(OAUTH_CODE_KEY, authorizationCode)
                .set(REDIRECT_URI_KEY, redirectUri)
                .build();

        HttpUrl url = account.getDomainUrl().newBuilder()
                .addPathSegment(OAUTH_PATH)
                .addPathSegment(TOKEN_PATH)
                .build();

        ParameterizableRequest<Credentials, AuthenticationException> request = httpClient.POST(url, Credentials.class);
        request.addParameters(parameters);
        request.useUrlencodedBody();
        return new TokenRequest(request);
    }
}
