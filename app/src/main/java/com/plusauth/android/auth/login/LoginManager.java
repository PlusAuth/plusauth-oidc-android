package com.plusauth.android.auth.login;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.plusauth.android.api.Api;
import com.plusauth.android.auth.AuthenticationActivity;
import com.plusauth.android.auth.AuthorizeResult;
import com.plusauth.android.auth.PKCE;
import com.plusauth.android.auth.ResumableManager;
import com.plusauth.android.auth.SimpleAuthCallback;
import com.plusauth.android.auth.exceptions.AuthenticationException;
import com.plusauth.android.crypto.CryptoUtil;
import com.plusauth.android.customtabs.CustomTabsOptions;
import com.plusauth.android.jwt.InvalidTokenException;
import com.plusauth.android.jwt.JWT;
import com.plusauth.android.jwt.TokenValidationOptions;
import com.plusauth.android.jwt.TokenValidator;
import com.plusauth.android.model.Account;
import com.plusauth.android.model.Credentials;
import com.plusauth.android.storage.CredentialsManager;
import com.plusauth.android.util.AuthenticationCallback;
import com.plusauth.android.util.UriHelper;
import com.plusauth.android.util.PLog;

import java.util.Date;
import java.util.Map;

/**
 * Manages OIDC login sessions. Should not be reused since it does not
 * reset internal state from previous request.
 */
public class LoginManager extends ResumableManager {
    static final String KEY_RESPONSE_TYPE = "response_type";
    static final String KEY_STATE = "state";
    static final String KEY_NONCE = "nonce";
    static final String RESPONSE_TYPE_ID_TOKEN = "id_token";
    static final String RESPONSE_TYPE_CODE = "code";
    static final String KEY_MAX_AGE = "max_age";

    private static final String ERROR_VALUE_AUTHENTICATION_CANCELED = "pa.authentication_canceled";
    private static final String METHOD_SHA_256 = "S256";
    private static final String KEY_CODE_CHALLENGE = "code_challenge";
    private static final String KEY_CODE_CHALLENGE_METHOD = "code_challenge_method";
    private static final String KEY_CLIENT_ID = "client_id";
    private static final String KEY_REDIRECT_URI = "redirect_uri";
    private static final String KEY_ERROR = "error";
    private static final String KEY_ERROR_DESCRIPTION = "error_description";
    private static final String KEY_ID_TOKEN = "id_token";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_TOKEN_TYPE = "token_type";
    private static final String KEY_EXPIRES_IN = "expires_in";
    private static final String KEY_CODE = "code";
    private static final String KEY_SCOPE = "scope";
    private static final String KEY_EXPIRES_AT = "expires_at";

    private final Account account;
    private final AuthenticationCallback callback;
    private final Api apiClient;
    private final int requestCode = 110;
    private Map<String, String> parameters;
    private PKCE pkce;
    private Long currentTimeInMillis; // for testing
    private CustomTabsOptions ctOptions;
    private CredentialsManager credentialsManager;
    private Long clockSkew;

    public LoginManager(@NonNull Account account, @NonNull CredentialsManager credentialsManager, @NonNull Api apiClient, @NonNull LoginRequest loginRequest, @NonNull AuthenticationCallback callback) {
        this.account = account;
        this.callback = callback;
        this.apiClient = apiClient;
        this.credentialsManager = credentialsManager;
        this.ctOptions = loginRequest.getCtOptions();
        this.parameters = loginRequest.getValues();
        this.clockSkew = loginRequest.getClockSkew();
    }

    /**
     * Merges given two credentials.
     *
     * @param urlCredentials  credentials from auth response
     * @param codeCredentials credentials from code exchange
     * @return merged credentials
     */
    @VisibleForTesting
    static Credentials mergeCredentials(Credentials urlCredentials, Credentials codeCredentials) {
        final String idToken = TextUtils.isEmpty(urlCredentials.getIdToken()) ? codeCredentials.getIdToken() : urlCredentials.getIdToken();
        final String accessToken = TextUtils.isEmpty(codeCredentials.getAccessToken()) ? urlCredentials.getAccessToken() : codeCredentials.getAccessToken();
        final String type = TextUtils.isEmpty(codeCredentials.getType()) ? urlCredentials.getType() : codeCredentials.getType();
        final String refreshToken = codeCredentials.getRefreshToken();
        final Date expiresAt = codeCredentials.getExpiresAt() != null ? codeCredentials.getExpiresAt() : urlCredentials.getExpiresAt();
        final String scope = TextUtils.isEmpty(codeCredentials.getScope()) ? urlCredentials.getScope() : codeCredentials.getScope();

        return new Credentials(idToken, accessToken, type, refreshToken, expiresAt.getTime(), scope);
    }

    /**
     * Launches CustomTab or browser for authentication flow.
     *
     * @param context     app/local context
     * @param redirectUri uri for response redirection, must be configured from oidc provider
     */
    public void start(Context context, String redirectUri) {
        addPKCEParameters(parameters, redirectUri);
        addClientParameters(parameters, redirectUri);
        addValidationParameters(parameters);
        Uri uri = buildAuthorizeUri();

        AuthenticationActivity.authenticate(context, uri, ctOptions);
    }


    /**
     * Completes authentication flow after CustomTab or browser is closed. If successful results
     * will be persisted to Storage.
     *
     * @param result authorization intent result
     */
    @Override
    public void resume(AuthorizeResult result) {
        if (result.isCanceled()) {
            AuthenticationException exception = new AuthenticationException(ERROR_VALUE_AUTHENTICATION_CANCELED, "The user closed the browser app and the authentication was canceled.");
            callback.onFailure(exception);
            return;
        }

        final Map<String, String> responseParameters = UriHelper.getQueryParamsFromUri(result.getIntentData());

        AuthenticationException ex = checkErrors(responseParameters);
        if (ex != null) {
            callback.onFailure(ex);
            return;
        }

        boolean idTokenExpected = parameters.containsKey(KEY_RESPONSE_TYPE) && parameters.get(KEY_RESPONSE_TYPE).contains(RESPONSE_TYPE_ID_TOKEN);
        Credentials credentials = resolveCredentialsFromResponse(idTokenExpected, responseParameters);

        if (idTokenExpected) {
            handleResponseIdToken(credentials, responseParameters);
            return;
        }

        if (!shouldUsePKCE()) {
            handleResponseToken(credentials);
            return;
        }

        handleResponseCode(credentials, responseParameters);
        return;
    }

    /**
     * Handles response_type: code * cases, exchanges auth code for credentials.
     *
     * @param urlCredentials     credentials from url
     * @param responseParameters parameters from response
     */
    private void handleResponseCode(final Credentials urlCredentials, Map<String, String> responseParameters) {
        PLog.d("Response_Type: code *, Fetching credentials using authorization code...");
        pkce.getToken(responseParameters.get(KEY_CODE), new SimpleAuthCallback(callback) {

            @Override
            public void onSuccess(@NonNull final Credentials credentials) {
                try {
                    validateCredentials(credentials);
                } catch (InvalidTokenException e) {
                    callback.onFailure(new AuthenticationException("Token validation failed.", e));
                    return;
                }

                Credentials finalCredentials = mergeCredentials(urlCredentials, credentials);
                saveCredentials(finalCredentials);
                callback.onSuccess(finalCredentials);
            }
        });
    }

    /**
     * Handles response_type token cases.
     *
     * @param urlCredentials credentials from url
     */
    private void handleResponseToken(Credentials urlCredentials) {
        credentialsManager.saveCredentials(urlCredentials);

        callback.onSuccess(urlCredentials);
    }

    /**
     * Handles response_type: id_token * cases, exchanges auth code for credentials if pkce was used.
     *
     * @param urlCredentials     credentials from url
     * @param responseParameters parameters from response
     */
    private void handleResponseIdToken(Credentials urlCredentials, Map<String, String> responseParameters) {
        try {
            validateCredentials(urlCredentials);
        } catch (InvalidTokenException e) {
            callback.onFailure(new AuthenticationException("Token validation failed.", e));
            return;
        }

        if (!shouldUsePKCE()) {
            credentialsManager.saveCredentials(urlCredentials);
            callback.onSuccess(urlCredentials);
            return;
        }

        handleResponseCode(urlCredentials, responseParameters);
    }

    /**
     * Checks to see if there are any errors in response
     *
     * @param responseParameters parameters from response
     * @return exception if there was an error, null otherwise
     */
    private AuthenticationException checkErrors(Map<String, String> responseParameters) {
        AuthenticationException ex = null;
        if (!TextUtils.isEmpty(responseParameters.get(KEY_ERROR))) {
            ex = new AuthenticationException(responseParameters.get(KEY_ERROR), responseParameters.get(KEY_ERROR_DESCRIPTION));
        } else if (!parameters.get(KEY_STATE).equals(responseParameters.get(KEY_STATE))) {
            ex = new AuthenticationException(String.format("State from response %s does not match state that was sent %s.", responseParameters.get(KEY_STATE), parameters.get(KEY_STATE)));
        }

        return ex;
    }

    /**
     * Checks to see if there are any errors with provided credentials id token.
     *
     * @param credentials credentials to be validated
     * @throws InvalidTokenException if credentials are invalid
     */
    private void validateCredentials(Credentials credentials) throws InvalidTokenException {
        JWT idToken = credentials.idTokenAsJWT();
        String domain = account.getDomainUrl().toString(); //https://x/, trailing slash must be removed before validation
        TokenValidationOptions opts = new TokenValidationOptions(domain.substring(0, domain.length() - 1), apiClient.getClientId());
        String maxAge = parameters.get(KEY_MAX_AGE);
        if (!TextUtils.isEmpty(maxAge)) {
            opts.maxAge = Long.valueOf(maxAge);
        }
        opts.clockSkew = clockSkew;
        opts.nonce = parameters.get(KEY_NONCE);

        TokenValidator.validate(idToken, opts);
    }

    /**
     * Returns credentials built from response parameters
     *
     * @param idTokenExpected
     * @param responseParameters params from response
     * @return resolved credentials
     */
    private Credentials resolveCredentialsFromResponse(boolean idTokenExpected, Map<String, String> responseParameters) {
        Long expiresAt = responseParameters.containsKey(KEY_EXPIRES_AT) ? Long.valueOf(responseParameters.get(KEY_EXPIRES_AT)) : null;

        if (expiresAt == null && responseParameters.containsKey(KEY_EXPIRES_IN)) {
            expiresAt = getCurrentTimeInMillis() + Long.parseLong(responseParameters.get(KEY_EXPIRES_IN));
        }

        String idToken = idTokenExpected ? responseParameters.get(KEY_ID_TOKEN) : null;

        return new Credentials(idToken, responseParameters.get(KEY_ACCESS_TOKEN), responseParameters.get(KEY_TOKEN_TYPE), null, expiresAt, responseParameters.get(KEY_SCOPE));
    }

    /**
     * Saves credentials to storage
     *
     * @param credentials credentials to be saved
     */
    private void saveCredentials(Credentials credentials) {
        credentialsManager.saveCredentials(credentials);
    }

    private long getCurrentTimeInMillis() {
        return currentTimeInMillis != null ? currentTimeInMillis : System.currentTimeMillis();
    }

    @VisibleForTesting
    void setCurrentTimeInMillis(long currentTimeInMillis) {
        this.currentTimeInMillis = currentTimeInMillis;
    }

    /**
     * Creates uri from url and params for login call.
     *
     * @return full uri containing all login url and params
     */
    private Uri buildAuthorizeUri() {
        String authorizeUrl = account.getDomainUrl().newBuilder()
                .addEncodedPathSegment("oauth")
                .addEncodedPathSegment("auth")
                .build()
                .toString();

        Uri authorizeUri = Uri.parse(authorizeUrl);
        Uri.Builder builder = authorizeUri.buildUpon();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            builder.appendQueryParameter(entry.getKey(), entry.getValue());
        }
        Uri uri = builder.build();
        PLog.d("Authorization URI: " + uri.toString());
        return uri;
    }

    /**
     * If available, adds pkce parameters to request.
     *
     * @param parameters  request params
     * @param redirectUri uri for response redirection, must be configured from oidc provider
     */
    private void addPKCEParameters(Map<String, String> parameters, String redirectUri) {
        if (!shouldUsePKCE()) {
            return;
        }
        pkce = new PKCE(apiClient, redirectUri);
        String codeChallenge = pkce.getCodeChallenge();
        parameters.put(KEY_CODE_CHALLENGE, codeChallenge);
        parameters.put(KEY_CODE_CHALLENGE_METHOD, METHOD_SHA_256);
        PLog.d("Using PKCE authentication flow");
    }

    /**
     * Adds state and nonce params if they are present in LoginRequest, if not generates them.
     *
     * @param parameters request params
     */
    private void addValidationParameters(Map<String, String> parameters) {
        String stateParam = parameters.get(KEY_STATE);
        String state = stateParam != null ? stateParam : CryptoUtil.secureRandomString();
        parameters.put(KEY_STATE, state);

        boolean idTokenExpected = parameters.containsKey(KEY_RESPONSE_TYPE) && (parameters.get(KEY_RESPONSE_TYPE).contains(RESPONSE_TYPE_ID_TOKEN) || parameters.get(KEY_RESPONSE_TYPE).contains(RESPONSE_TYPE_CODE));
        if (idTokenExpected) {
            String nonceParam = parameters.get(KEY_NONCE);
            String nonce = nonceParam != null ? nonceParam : CryptoUtil.secureRandomString();
            parameters.put(KEY_NONCE, nonce);
        }
    }

    /**
     * Adds client_id and redirect_uri params.
     *
     * @param parameters request params
     * @param redirectUri uri for response redirection, must be configured from oidc provider
     */
    private void addClientParameters(Map<String, String> parameters, String redirectUri) {
        parameters.put(KEY_CLIENT_ID, account.getClientId());
        parameters.put(KEY_REDIRECT_URI, redirectUri);
    }

    /**
     * Checks if pcke is available on device and response_type includes 'code'
     *
     * @return whether should use pkce
     */
    private boolean shouldUsePKCE() {
        return parameters.containsKey(KEY_RESPONSE_TYPE) && parameters.get(KEY_RESPONSE_TYPE).contains(RESPONSE_TYPE_CODE) && PKCE.isAvailable();
    }

}
