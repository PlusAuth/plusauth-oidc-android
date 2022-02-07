package com.plusauth.android.api.request;


import com.plusauth.android.auth.exceptions.AuthenticationException;
import com.plusauth.android.model.Credentials;

import java.util.Map;

/**
 * Request to authenticate a user with OIDC api
 */
public interface AuthenticationRequest extends Request<Credentials, AuthenticationException> {

    /**
     * Sets the 'grant_type' parameter
     *
     * @param grantType value
     * @return this
     */
    AuthenticationRequest setGrantType(String grantType);

    /**
     * Sets the 'scope' parameter.
     *
     * @param scope value
     * @return this
     */
    AuthenticationRequest setScope(String scope);

    /**
     * Sets the 'audience' parameter.
     *
     * @param audience value
     * @return this
     */
    AuthenticationRequest setAudience(String audience);

    /**
     * Sets the 'access_token' parameter
     *
     * @param accessToken value
     * @return this
     */
    AuthenticationRequest setAccessToken(String accessToken);

    /**
     * Add all entries of the map as parameters of this request
     *
     * @param parameters to be added to the request
     * @return this
     */
    AuthenticationRequest addParameters(Map<String, Object> parameters);

    /**
     * Adds given values as request header.
     *
     * @param name name of the header
     * @param value value of the header
     * @return this
     */
    AuthenticationRequest addHeader(String name, String value);
}
