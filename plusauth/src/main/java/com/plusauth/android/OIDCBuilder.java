package com.plusauth.android;

import android.content.Context;

import androidx.annotation.NonNull;

import com.plusauth.android.api.Api;
import com.plusauth.android.api.internal.GsonFactory;
import com.plusauth.android.api.internal.OkHttpClientFactory;
import com.plusauth.android.api.request.PAHttpClient;
import com.plusauth.android.crypto.Encryptor;
import com.plusauth.android.crypto.VoidEncryptor;
import com.plusauth.android.model.Account;
import com.plusauth.android.storage.CredentialsManager;
import com.plusauth.android.storage.SharedPreferencesStorage;
import com.plusauth.android.storage.Storage;
import com.plusauth.android.util.PLog;
import com.plusauth.android.util.UriHelper;

import okhttp3.OkHttpClient;

public class OIDCBuilder {
    private Encryptor encryptor;
    private Account account;
    private CredentialsManager credentialsManager;
    private Storage storage;
    private boolean loggingEnabled;


    /**
     * Builder for OIDC. We recommend utilizing singleton or DI patterns for easier handling of the
     * resulting OIDC instance.
     *
     * @param context activity/application context, library never stores context instances to prevent leaks
     * @param clientId id of your OIDC native client
     * @param domain your OIDC domain ex. starter.plusauth.com
     */
    public OIDCBuilder(@NonNull Context context, @NonNull String clientId, @NonNull String domain) {
        String redirectUri = UriHelper.getCallbackUri(context.getPackageName());
        this.account = new Account(clientId, domain, redirectUri);
        this.storage = new SharedPreferencesStorage(context);
        this.encryptor = new VoidEncryptor();
    }

    /**
     * Enables logging for the library. This includes debug, http and error(cases where exceptions are omitted) logs.
     * @param loggingEnabled boolean value determining logging state
     * @return this
     */
    public OIDCBuilder setLoggingEnabled(boolean loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
        return this;
    }

    /**
     * Configures where the credentials will be stored. Default storage is {@link SharedPreferencesStorage}
     * @param storage object that implements Storage interface
     * @return this
     */
    public OIDCBuilder setStorage(@NonNull Storage storage) {
        this.storage = storage;
        return this;
    }

    /**
     * Enables encryption of credentials before they are saved to storage. The default storage
     * {@link SharedPreferencesStorage} runs in private mode which provides a good level of security.
     * Library provides you with the {@link com.plusauth.android.crypto.AESEncryptor} class which you are
     * welcome to use.
     *
     * @param encryptor object that implements Encryptor interface
     * @return this
     */
    public OIDCBuilder setEncryptor(Encryptor encryptor) {
        this.encryptor = encryptor;
        return this;
    }

    /**
     * Completes building process of OIDC object.
     * @return OIDC object that is configured with options you have chosen
     */
    public OIDC build() {
        PLog.setEnabled(loggingEnabled);

        OkHttpClient okHttpClient = new OkHttpClientFactory().createClient(loggingEnabled);

        PAHttpClient httpClient = new PAHttpClient(okHttpClient, GsonFactory.build());
        Api api = new Api(account, httpClient);
        this.credentialsManager = new CredentialsManager(api, storage, encryptor);
        api.setCredentialsManager(credentialsManager);

        return new OIDC(account, credentialsManager, api);
    }
}
