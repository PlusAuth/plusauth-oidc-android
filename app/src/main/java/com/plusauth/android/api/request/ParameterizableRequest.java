package com.plusauth.android.api.request;

import com.plusauth.android.auth.exceptions.AuthenticationException;

import java.util.Map;

/**
 * @param <T> the type that will be returned on success
 * @param <U>the type that will be returned on failure
 */
public interface ParameterizableRequest<T, U extends AuthenticationException> extends Request<T, U> {

    /**
     * Add all entries of the map as parameters of this request.
     *
     * @param parameters parameters to be added
     * @return this
     */
    ParameterizableRequest<T, U> addParameters(Map<String, Object> parameters);

    /**
     * Add entry as parameter of this request.
     *
     * @param name param name
     * @param value param value
     * @return this
     */
    ParameterizableRequest<T, U> addParameter(String name, Object value);

    /**
     * Add entry as header of this request.
     *
     * @param name header name
     * @param value header value
     * @return this
     */
    ParameterizableRequest<T, U> addHeader(String name, String value);

    /**
     * Sends the body as urlencoded instead of json.
     *
     * @return this
     */
    ParameterizableRequest<T, U> useUrlencodedBody();
}
