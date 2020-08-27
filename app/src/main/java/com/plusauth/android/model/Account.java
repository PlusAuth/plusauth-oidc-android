package com.plusauth.android.model;

import android.content.Intent;

import androidx.annotation.Nullable;

import com.plusauth.android.auth.AuthorizeResult;
import com.plusauth.android.auth.ResumableManager;
import com.plusauth.android.util.PLog;

import okhttp3.HttpUrl;


/**
 * Class for holding client details.
 */
public class Account {
    private static ResumableManager manager;

    private final String clientId;
    private final HttpUrl domainUrl;
    private final String redirectUri;
    private boolean loggingEnabled;

    public Account(String clientId, String domain, String redirectUri) {
        this.clientId = clientId;
        this.domainUrl = createHttpUrl(domain);
        this.redirectUri = redirectUri;
    }

    /**
     * Finalizes authentication flows by calling latest Login/Logout Manager with results coming
     * from AuthenticationActivity. Objects are difficult to pass between activities which is the
     * reason this method is used.
     *
     * @param intent intent from auth activity
     */
    public static void resume(@Nullable Intent intent) {
        if (manager == null) {
            PLog.e("There is no previous instance of this provider. How could this happen?");
            return;
        }

        final AuthorizeResult result = new AuthorizeResult(intent);
        manager.resume(result);
        setManager(null);
    }

    /**
     * Setter for manager to be used after authorization flows.
     *
     * @param manager resumable manage
     */
    public static void setManager(ResumableManager manager) {
        Account.manager = manager;
    }

    /**
     * Creates HttpUrl from url. If url does not start with http, prepends https:// to it.
     *
     * @param url url to be converted
     * @return converted HttpUrl
     */
    private HttpUrl createHttpUrl(String url) {
        if (url == null) {
            return null;
        }
        String fullUrl = url.startsWith("http") ? url : "https://" + url;
        return HttpUrl.parse(fullUrl);
    }

    /**
     * Checks if logging for library is enabled
     *
     * @return whether logging for library is enabled
     */
    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    /**
     * Setter for enabling logging for library. Do not enable logging
     * in production as it logs http bodies including auth tokens.
     *
     * @param loggingEnabled whether logging should be enabled
     */
    public void setLoggingEnabled(boolean loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
    }

    /**
     * Getter for domain url.
     *
     * @return HttpUrl of domain url
     */
    public HttpUrl getDomainUrl() {
        return domainUrl;
    }

    /**
     * Getter for client id.
     *
     * @return client id
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Getter for redirect uri.
     *
     * @return redirect uri
     */
    public String getRedirectUri() {
        return redirectUri;
    }
}
