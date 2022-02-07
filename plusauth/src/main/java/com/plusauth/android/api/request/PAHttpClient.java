package com.plusauth.android.api.request;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.plusauth.android.auth.exceptions.AuthenticationException;

import java.util.HashMap;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

/**
 * Wrapper class for OkHttpClient, provides simpler interface for creating http calls.
 */
public class PAHttpClient {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String ACCEPT_LANGUAGE_HEADER = "Accept-Language";

    private final HashMap<String, String> headers;
    private final OkHttpClient client;
    private final Gson gson;

    public PAHttpClient(OkHttpClient client, Gson gson) {
        this.client = client;
        this.gson = gson;
        headers = new HashMap<>();
    }

    public PAHttpClient(OkHttpClient client, Gson gson, @NonNull String bearerToken) {
        this(client, gson);
        headers.put(AUTHORIZATION_HEADER, "Bearer " + bearerToken);
    }

    /**
     * Sets 'Accept-Language' header of all requests from this client.
     *
     * @param locale ISO code of locale e.g. en_US
     */
    public void setLocale(String locale) {
        headers.put(ACCEPT_LANGUAGE_HEADER, locale);
    }

    /**
     * Creates a POST request.
     *
     * @param url where to send the request
     * @param returnType type of successful result
     * @param <T> type of successful result
     * @return request call
     */
    public <T> ParameterizableRequest<T, AuthenticationException> POST(HttpUrl url, Class<T> returnType) {
        return createSimpleRequest(url, "POST", returnType);
    }

    /**
     * Creates a POST request with Void response type.
     *
     * @param url where to send the request
     * @return request call
     */
    public ParameterizableRequest<Void, AuthenticationException> POST(HttpUrl url) {
        return createVoidRequest(url, "POST");
    }

    /**
     * Creates a PATCH request.
     *
     * @param url where to send the request
     * @param returnType type of successful result
     * @param <T> type of successful result
     * @return request call
     */
    public <T> ParameterizableRequest<T, AuthenticationException> PATCH(HttpUrl url, Class<T> returnType) {
        return createSimpleRequest(url, "PATCH", returnType);
    }

    /**
     * Creates a DELETE request.
     *
     * @param url where to send the request
     * @param returnType type of successful result
     * @param <T> type of successful result
     * @return request call
     */
    public <T> ParameterizableRequest<T, AuthenticationException> DELETE(HttpUrl url, Class<T> returnType) {
        return createSimpleRequest(url, "DELETE", returnType);
    }

    /**
     * Creates a GET request.
     *
     * @param url where to send the request
     * @param returnType type of successful result
     * @param <T> type of successful result
     * @return request call
     */
    public <T> ParameterizableRequest<T, AuthenticationException> GET(HttpUrl url, Class<T> returnType) {
        return createSimpleRequest(url, "GET", returnType);
    }

    /**
     * Creates a request.
     *
     * @param url where to send the request
     * @param returnType type of successful result
     * @param <T> type of successful result
     * @return request call
     */
    <T> ParameterizableRequest<T, AuthenticationException> createSimpleRequest(HttpUrl url, String method, Class<T> returnType) {
        return new SimpleRequest<T>(url, client, gson, method, returnType);
    }

    /**
     * Creates a request with Void response type.
     *
     * @param url where to send the request
     * @return request call
     */
    ParameterizableRequest<Void, AuthenticationException> createVoidRequest(HttpUrl url, String method) {
        return new VoidRequest(url, client, gson, method);
    }
}
