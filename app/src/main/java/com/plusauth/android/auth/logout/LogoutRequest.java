package com.plusauth.android.auth.logout;

import androidx.annotation.NonNull;

import com.plusauth.android.customtabs.CustomTabsOptions;

public class LogoutRequest {

    private CustomTabsOptions ctOptions;
    private String idToken;

    /**
     * When using a Custom Tabs compatible Browser, apply these customization options.
     *
     * @param options the Custom Tabs customization options
     * @return the current builder instance
     */
    public LogoutRequest setCustomTabsOptions(@NonNull CustomTabsOptions options) {
        this.ctOptions = options;
        return this;
    }

    public LogoutRequest setIdToken(@NonNull String idToken) {
        this.idToken = idToken;
        return this;
    }

    CustomTabsOptions getCtOptions() {
        return ctOptions;
    }

    String getIdToken() {
        return idToken;
    }
}