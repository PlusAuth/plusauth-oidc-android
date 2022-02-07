package com.plusauth.android.auth.logout;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.plusauth.android.auth.AuthenticationActivity;
import com.plusauth.android.auth.AuthorizeResult;
import com.plusauth.android.auth.ResumableManager;
import com.plusauth.android.auth.exceptions.AuthenticationException;
import com.plusauth.android.customtabs.CustomTabsOptions;
import com.plusauth.android.model.Account;
import com.plusauth.android.storage.CredentialsManager;
import com.plusauth.android.util.PLog;
import com.plusauth.android.util.VoidCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages OIDC logout sessions. Should not be reused since it does not
 *  * reset internal state from previous request.
 */
public class LogoutManager extends ResumableManager {

    private static final String KEY_ID_TOKEN_HINT = "id_token_hint";
    private static final String KEY_RETURN_TO_URL = "post_logout_redirect_uri";
    private static final String BROWSER_CLOSED = "pa.browser.closed";
    private static final String NO_ID_TOKEN_HINT = "pa.id_token.not_found";

    private final Account account;
    private final VoidCallback callback;
    private final Map<String, String> parameters;
    private final CredentialsManager credentialsManager;
    private final LogoutRequest logoutRequest;

    public LogoutManager(@NonNull Account account, @NonNull CredentialsManager credentialsManager, LogoutRequest logoutRequest, @NonNull VoidCallback callback) {
        this.account = account;
        this.callback = callback;
        this.parameters = new HashMap<>();
        this.credentialsManager = credentialsManager;
        this.logoutRequest = logoutRequest;
    }

    /**
     * Launches CustomTab or browser for authentication flow. If an id token was not provided and
     * Storage does not have a valid id token, callback will be called with an exception.
     *
     * @param context     app/local context
     * @param redirectUri uri for response redirection, must be configured from oidc provider
     */
    public void start(Context context, String redirectUri) {
        if (getIdToken() == null) {
            callback.onFailure(new AuthenticationException(NO_ID_TOKEN_HINT, "No id token found in storage, you must provide one with LogoutRequest."));
        }

        addClientParameters(parameters, redirectUri);
        Uri uri = buildLogoutUri();

        AuthenticationActivity.authenticate(context, uri, logoutRequest.getCtOptions());
    }

    /**
     * Completes authentication flow after CustomTab or browser is closed. If successful
     * results will be persisted to Storage.
     *
     * @param result authorization intent result
     */
    @Override
    public void resume(AuthorizeResult result) {
        if (result.isCanceled()) {
            AuthenticationException exception = new AuthenticationException(BROWSER_CLOSED, "The user closed the browser app so the logout was cancelled.");
            callback.onFailure(exception);
        } else {
            credentialsManager.clearCredentials();
            callback.onSuccess(null);
        }
    }

    /**
     * Creates uri from url and params for logout call.
     *
     * @return full uri containing all logout url and params
     */
    private Uri buildLogoutUri() {
        String logoutUrl = account.getDomainUrl().newBuilder()
                .addEncodedPathSegment("logout")
                .build()
                .toString();

        Uri logoutUri = Uri.parse(logoutUrl);
        Uri.Builder builder = logoutUri.buildUpon();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            builder.appendQueryParameter(entry.getKey(), entry.getValue());
        }
        Uri uri = builder.build();
        PLog.d("Using the following Logout URI: " + uri.toString());
        return uri;
    }

    /**
     * Returns id token provided with LogoutRequest, if null will return id token
     * of credentials from Storage.
     *
     * @return id token
     */
    private String getIdToken() {
        String idToken = null;
        if (logoutRequest.getIdToken() != null) idToken = logoutRequest.getIdToken();
        else if (credentialsManager.getCredentialsWithoutValidation() != null)
            idToken = credentialsManager.getCredentialsWithoutValidation().getIdToken();

        return idToken;
    }

    /**
     * Adds id_token and post_logout_redirect_uri params to request.
     *
     * @param parameters params to add to
     * @param redirectUri uri for response redirection, must be configured from oidc provider
     */
    private void addClientParameters(Map<String, String> parameters, String redirectUri) {
        parameters.put(KEY_ID_TOKEN_HINT, getIdToken());
        parameters.put(KEY_RETURN_TO_URL, redirectUri);
    }

    @VisibleForTesting
    CustomTabsOptions customTabsOptions() {
        return logoutRequest.getCtOptions();
    }

}
