package com.plusauth.android.api;


import com.plusauth.android.BaseException;
import com.plusauth.android.api.request.ParameterizableRequest;
import com.plusauth.android.api.request.Request;
import com.plusauth.android.auth.exceptions.AuthenticationException;
import com.plusauth.android.model.Credentials;
import com.plusauth.android.util.AuthenticationCallback;
import com.plusauth.android.util.PACallback;

import java.util.Map;

/**
 * OIDC token request for getting/exchanging tokens.
 */
public class TokenRequest implements Request<Credentials, AuthenticationException> {

    private static final String OAUTH_CODE_VERIFIER_KEY = "code_verifier";

    private final ParameterizableRequest<Credentials, AuthenticationException> request;

    public TokenRequest(ParameterizableRequest<Credentials, AuthenticationException> request) {
        this.request = request;
    }

    public TokenRequest addParameters(Map<String, Object> parameters) {
        request.addParameters(parameters);
        return this;
    }

    public TokenRequest addHeader(String name, String value) {
        request.addHeader(name, value);
        return this;
    }

    /**
     * Add code verifier parameter to token request. A challenge must have been sent with previous authorize
     * request.
     *
     * @param codeVerifier the code verifier used to generate the challenge
     * @return this
     */
    public TokenRequest setCodeVerifier(String codeVerifier) {
        this.request.addParameter(OAUTH_CODE_VERIFIER_KEY, codeVerifier);
        return this;
    }

    @Override
    public void call(PACallback<Credentials, AuthenticationException> callback) {
        request.call(callback);
    }

    @Override
    public Credentials callSync() throws BaseException {
        return request.callSync();
    }
}
