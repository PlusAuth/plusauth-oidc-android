package com.plusauth.android.auth.login;

import androidx.annotation.NonNull;

import com.plusauth.android.auth.ResponseType;
import com.plusauth.android.customtabs.CustomTabsOptions;

import java.util.HashMap;
import java.util.Map;

import static com.plusauth.android.auth.login.LoginManager.KEY_MAX_AGE;
import static com.plusauth.android.auth.login.LoginManager.KEY_NONCE;
import static com.plusauth.android.auth.login.LoginManager.KEY_RESPONSE_TYPE;
import static com.plusauth.android.auth.login.LoginManager.KEY_STATE;
import static com.plusauth.android.auth.login.LoginManager.RESPONSE_TYPE_CODE;
import static com.plusauth.android.auth.login.LoginManager.RESPONSE_TYPE_ID_TOKEN;

/**
 * This class is used for configuring OIDC login. By default
 * uses authorization code flow with pkce for maximum security.
 */
public class LoginRequest {
    private static final String KEY_AUDIENCE = "audience";
    private static final String KEY_SCOPE = "scope";
    private static final String RESPONSE_TYPE_TOKEN = "token";

    private final Map<String, String> values = new HashMap<>();
    private CustomTabsOptions ctOptions;
    private long clockSkew;

    public LoginRequest() {
        //Default values
        setResponseType(ResponseType.CODE);
        setScope("openid offline_access");
        setClockSkew(1000L);
    }

    /**
     * Sets state for login request. If not provided
     * one will be generated for you.
     *
     * @param state value to be used
     * @return this
     */
    public LoginRequest setState(@NonNull String state) {
        this.values.put(KEY_STATE, state);
        return this;
    }


    /**
     * Sets nonce for login request. If not provided
     * one will be generated for you.
     *
     * @param nonce value to be used
     * @return this
     */
    public LoginRequest setNonce(@NonNull String nonce) {
        this.values.put(KEY_NONCE, nonce);
        return this;
    }

    /**
     * Sets audience for login request.
     *
     * @param audience value to be used
     * @return this
     */
    public LoginRequest setAudience(@NonNull String audience) {
        this.values.put(KEY_AUDIENCE, audience);
        return this;
    }

    /**
     * Sets max_age for login request.
     *
     * @param maxAge value to be used
     * @return maxAge
     */
    public LoginRequest setMaxAge(int maxAge) {
        this.values.put(KEY_MAX_AGE, String.valueOf(maxAge));
        return this;
    }

    /**
     * Sets scope for login request.
     *
     * @param scope value to be used
     * @return this
     */
    public LoginRequest setScope(@NonNull String scope) {
        this.values.put(KEY_SCOPE, scope);
        return this;
    }

    /**
     * Sets response_type for login request. Defaults to ResponseType.CODE
     * for maximum security together with pkce.
     *
     * @param type value to be used, multiple values will be concatenated according to spec
     * @return this
     */
    public LoginRequest setResponseType(ResponseType... type) {
        StringBuilder sb = new StringBuilder();

        for (ResponseType responseType : type) {
            if (responseType == ResponseType.CODE) {
                sb.append(RESPONSE_TYPE_CODE).append(" ");
            }
            if (responseType == ResponseType.ID_TOKEN) {
                sb.append(RESPONSE_TYPE_ID_TOKEN).append(" ");
            }
            if (responseType == ResponseType.TOKEN) {
                sb.append(RESPONSE_TYPE_TOKEN).append(" ");
            }
        }

        this.values.put(KEY_RESPONSE_TYPE, sb.toString().trim());
        return this;
    }

    /**
     * Adds extra parameters to login request. Make sure your provider allows this.
     *
     * @param parameters to add
     * @return this
     */
    public LoginRequest setParameters(@NonNull Map<String, Object> parameters) {
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            if (entry.getValue() != null) {
                this.values.put(entry.getKey(), entry.getValue().toString());
            }
        }
        return this;
    }


    /**
     * If system supports CustomTabs, these options will be used to customize it.
     *
     * @param options value to be used
     * @return this
     */
    public LoginRequest setCustomTabsOptions(@NonNull CustomTabsOptions options) {
        this.ctOptions = options;
        return this;
    }

    Map<String, String> getValues() {
        return values;
    }

    CustomTabsOptions getCtOptions() {
        return ctOptions;
    }

    public long getClockSkew() {
        return clockSkew;
    }

    /**
     * Sets the allowed clock skew that will be used for token expiry validation.
     *
     * @param clockSkew value to be used
     * @return this
     */
    public LoginRequest setClockSkew(long clockSkew) {
        this.clockSkew = clockSkew;
        return this;
    }
}
