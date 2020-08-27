package com.plusauth.android;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.plusauth.android.api.Api;
import com.plusauth.android.auth.exceptions.AuthenticationException;
import com.plusauth.android.auth.exceptions.NoAvailableBrowserException;
import com.plusauth.android.auth.login.LoginManager;
import com.plusauth.android.auth.login.LoginRequest;
import com.plusauth.android.auth.logout.LogoutManager;
import com.plusauth.android.auth.logout.LogoutRequest;
import com.plusauth.android.model.Account;
import com.plusauth.android.model.Credentials;
import com.plusauth.android.storage.CredentialsManager;
import com.plusauth.android.storage.CredentialsManagerException;
import com.plusauth.android.util.AuthenticationCallback;
import com.plusauth.android.util.UriHelper;
import com.plusauth.android.util.PACallback;
import com.plusauth.android.util.PLog;
import com.plusauth.android.util.VoidCallback;


public class OIDC {
    private final Account account;
    private CredentialsManager credentialsManager;
    private Api api;

    public OIDC(Account account, CredentialsManager credentialsManager, Api api) {
        this.account = account;
        this.credentialsManager = credentialsManager;
        this.api = api;
    }

    /**
     * @return CredentialsManager which is a wrapper around Storage+Encryptor
     */
    public CredentialsManager getCredentialsManager() {
        return credentialsManager;
    }

    /**
     * @return Api client that you can use for advanced use cases
     */
    public Api getApi() {
        return api;
    }


    /**
     * Starts an async login session. If there are valid credentials in Storage they will be returned
     * instead, preventing OIDC exchange. OIDC will open a browser custom tab(if supported) showing
     * them your OIDC login/register page. After successful response credentials will be locally
     * stored to skip the OIDC flow at next login.
     *
     *
     * @param context activity/application context, library never stores context instances to prevent leaks
     * @param loginRequest object containing login options for OIDC
     * @param callback results of this call will be delivered through this object
     */
    public void login(@NonNull Context context, @NonNull LoginRequest loginRequest, @NonNull AuthenticationCallback callback) {
        if (credentialsManager.hasValidCredentials()) {
            PLog.d("Storage has valid credentials, attempting to fetch them...");

            credentialsManager.getCredentials(new PACallback<Credentials, CredentialsManagerException>() {
                @Override
                public void onSuccess(Credentials payload) {
                    callback.onSuccess(payload);
                }

                @Override
                public void onFailure(CredentialsManagerException error) {
                    callback.onFailure(new AuthenticationException("Failed to fetch existing credentials from storage.", error));
                }
            });

            return;
        }

        PLog.d("Storage does not have valid credentials, launching login page...");

        Account.setManager(null);

        if (!hasBrowserAppInstalled(context.getPackageManager())) {
            final NoAvailableBrowserException ex = new NoAvailableBrowserException();
            callback.onFailure(ex);
            return;
        }
        LoginManager manager = new LoginManager(account, credentialsManager, api, loginRequest, callback);

        Account.setManager(manager);

        manager.start(context, account.getRedirectUri());
    }

    /**
     * Starts an async logout session. A browser custom tab will be launched momentarily for
     * full sign out (local + sso) then the user will back to the app.
     * If local storage does not have any id tokens and you don't provide one
     * with the request an exception will be thrown.
     *
     * @param context activity/application context, library never stores context instances to prevent leaks
     * @param logoutRequest object containing logout options for OIDC
     * @param callback results of this call will be delivered through this object
     */
    public void logout(@NonNull Context context, @NonNull LogoutRequest logoutRequest, @NonNull VoidCallback callback) {
        Account.setManager(null);

        if (!hasBrowserAppInstalled(context.getPackageManager())) {
            final NoAvailableBrowserException ex = new NoAvailableBrowserException();
            callback.onFailure(ex);
            return;
        }


        LogoutManager logoutManager = new LogoutManager(account, credentialsManager, logoutRequest, callback);

        Account.setManager(logoutManager);
        logoutManager.start(context, account.getRedirectUri());
    }

    /**
     * Checks if there are any browser apps installed on the phone.
     *
     * @param packageManager package manager instance
     * @return whether a browser is installed
     */
    private static boolean hasBrowserAppInstalled(@NonNull PackageManager packageManager) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com"));
        return intent.resolveActivity(packageManager) != null;
    }

}
