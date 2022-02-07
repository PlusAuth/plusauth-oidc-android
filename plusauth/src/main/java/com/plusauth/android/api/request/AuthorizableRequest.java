package com.plusauth.android.api.request;

import com.plusauth.android.auth.exceptions.AuthenticationException;

/**
 * Interface for a OIDC requests that requires authorization.
 *
 * @param <T> the type this request will return on success.
 * @param <U> the type this request will return on failure.
 */
public interface AuthorizableRequest<T, U extends AuthenticationException> extends ParameterizableRequest<T, U> {

    /**
     * Set the JWT used in 'Authorization' header value
     *
     * @param jwt token to send. Do not include the 'Bearer' keyword
     * @return this
     */
    AuthorizableRequest<T, U> setBearer(String jwt);

}
