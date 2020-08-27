package com.plusauth.android.api;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Convenience class for preparing request parameters. Provides methods for
 * common oidc parameters.
 */
public class MapBuilder {
    public static final String SCOPE_KEY = "scope";
    public static final String REFRESH_TOKEN_KEY = "refresh_token";
    public static final String ACCESS_TOKEN_KEY = "access_token";
    public static final String CLIENT_ID_KEY = "client_id";
    public static final String GRANT_TYPE_KEY = "grant_type";
    public static final String AUDIENCE_KEY = "audience";

    private Map<String, Object> parameters;

    private MapBuilder(@NonNull Map<String, Object> parameters) {
        this.parameters = new HashMap<>(parameters);
    }

    /**
     * Creates a new instance of the builder.
     * This builder wont have any default values
     *
     * @return a new builder
     */
    public static MapBuilder newBuilder() {
        return newBuilder(new HashMap<>());
    }

    /**
     * Creates a new instance of the builder from some initial parameters.
     *
     * @param parameters initial parameters
     * @return a new builder
     */
    public static MapBuilder newBuilder(Map<String, Object> parameters) {
        return new MapBuilder(parameters);
    }

    /**
     * Sets the 'client_id' parameter
     *
     * @param clientId the application's client id
     * @return this
     */
    public MapBuilder setClientId(String clientId) {
        return set(CLIENT_ID_KEY, clientId);
    }

    /**
     * Sets the 'grant_type' parameter
     *
     * @param grantType grant type
     * @return this
     */
    public MapBuilder setGrantType(String grantType) {
        return set(GRANT_TYPE_KEY, grantType);
    }

    /**
     * Sets the 'scope' parameter.
     *
     * @param scope a scope value
     * @return this
     */
    public MapBuilder setScope(String scope) {
        return set(SCOPE_KEY, scope);
    }

    /**
     * Sets the 'audience' parameter.
     *
     * @param audience an audience value
     * @return this
     */
    public MapBuilder setAudience(String audience) {
        return set(AUDIENCE_KEY, audience);
    }

    /**
     * Sets the 'access_token' parameter
     *
     * @param accessToken a access token
     * @return this
     */
    public MapBuilder setAccessToken(String accessToken) {
        return set(ACCESS_TOKEN_KEY, accessToken);
    }

    /**
     * Sets the 'refresh_token' parameter
     *
     * @param refreshToken a refresh token
     * @return this
     */
    public MapBuilder setRefreshToken(String refreshToken) {
        return set(REFRESH_TOKEN_KEY, refreshToken);
    }

    /**
     * Creates and returns an immutable map with all the parameters
     *
     * @return all parameters
     */
    public Map<String, Object> build() {
        return Collections.unmodifiableMap(new HashMap<>(this.parameters));
    }

    /**
     * Sets a param
     *
     * @param key   param name
     * @param value param value. If null will remove set key.
     * @return this
     */
    public MapBuilder set(String key, Object value) {
        if (value == null) {
            this.parameters.remove(key);
        } else {
            this.parameters.put(key, value);
        }
        return this;
    }

    /**
     * Adds all parameter from a map
     *
     * @param parameters entries, will skip null values
     * @return this
     */
    public MapBuilder addAll(Map<String, Object> parameters) {
        for (String key : parameters.keySet()) {
            Object value = parameters.get(key);
            if (value != null) {
                this.parameters.put(key, value);
            }
        }
        return this;
    }

    /**
     * Clears all parameters
     *
     * @return this
     */
    public MapBuilder clearAll() {
        parameters.clear();
        return this;
    }

}
