package com.plusauth.android.api.request;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.plusauth.android.api.MapBuilder;
import com.plusauth.android.api.NetworkErrorException;
import com.plusauth.android.api.RequestBodyBuildException;
import com.plusauth.android.api.internal.JsonRequestBodyBuilder;
import com.plusauth.android.api.internal.UrlencodedRequestBodyBuilder;
import com.plusauth.android.auth.exceptions.AuthenticationException;
import com.plusauth.android.util.PACallback;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Base wrapper for OkHttp Callback interface. Provides sane defaults and utilities.
 *
 * @param <T> type that will be returned on successful responses
 */
public abstract class BaseRequest<T> implements ParameterizableRequest<T, AuthenticationException>, AuthorizableRequest<T, AuthenticationException>, Callback {

    protected final HttpUrl url;
    protected final OkHttpClient client;
    private final Map<String, String> headers;
    private final TypeAdapter<T> adapter;
    private final Gson gson;
    private final MapBuilder builder;
    private boolean useUrlencoding;
    private PACallback<T, AuthenticationException> callback;

    public BaseRequest(HttpUrl url, OkHttpClient client, Gson gson, TypeAdapter<T> adapter) {
        this(url, client, gson, adapter, null);
    }

    public BaseRequest(HttpUrl url, OkHttpClient client, Gson gson, TypeAdapter<T> adapter, PACallback<T, AuthenticationException> callback) {
        this(url, client, gson, adapter, callback, new HashMap<>(), MapBuilder.newBuilder());
    }

    @VisibleForTesting
    BaseRequest(HttpUrl url, OkHttpClient client, Gson gson, TypeAdapter<T> adapter, PACallback<T, AuthenticationException> callback, Map<String, String> headers, MapBuilder mapBuilder) {
        this.url = url;
        this.client = client;
        this.gson = gson;
        this.adapter = adapter;
        this.callback = callback;
        this.headers = headers;
        this.builder = mapBuilder;
    }

    /**
     * Wrapper for sending successful response.
     *
     * @param payload payload to be posted on success
     */
    protected void postOnSuccess(final T payload) {
        this.callback.onSuccess(payload);
    }

    /**
     * Wrapper for sending failed response.
     *
     * @param error payload to be posted on failure
     */
    protected final void postOnFailure(final AuthenticationException error) {
        this.callback.onFailure(error);
    }

    /**
     * Creates an instance of Request.Builder and initializes it with
     * url and headers.
     *
     * @return builder
     */
    protected Request.Builder newBuilder() {
        final Request.Builder builder = new Request.Builder()
                .url(url);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
        return builder;
    }

    /**
     * Getter for type adapter
     *
     * @return type adapter
     */
    protected TypeAdapter<T> getAdapter() {
        return adapter;
    }

    @VisibleForTesting
    PACallback<T, AuthenticationException> getCallback() {
        return callback;
    }

    /**
     * Callback to be used on response.
     *
     * @param callback value
     */
    protected void setCallback(PACallback<T, AuthenticationException> callback) {
        this.callback = callback;
    }

    /**
     * Encodes body parameters and converts it to OkHttp's RequestBody.
     *
     * @return encoded request body or null if there is no body
     * @throws RequestBodyBuildException if body is malformed
     */
    protected RequestBody buildBody() throws RequestBodyBuildException {
        Map<String, Object> dictionary = builder.build();
        if (!dictionary.isEmpty()) {
            if (useUrlencoding)
                return UrlencodedRequestBodyBuilder.createBody(dictionary);
            else
                return JsonRequestBodyBuilder.createBody(dictionary, gson);
        }
        return null;
    }

    /**
     * Converts failed response body to exception.
     *
     * @param response response from server
     * @return exception created from response
     */
    protected AuthenticationException parseUnsuccessfulResponse(Response response) {
        String stringPayload = null;
        try (ResponseBody body = response.body()) {
            stringPayload = body.string();
            Type mapType = new TypeToken<Map<String, Object>>() {
            }.getType();
            Map<String, Object> mapPayload = gson.fromJson(stringPayload, mapType);
            return new AuthenticationException(mapPayload);
        } catch (JsonSyntaxException e) {
            return new AuthenticationException(stringPayload, response.code());
        } catch (IOException e) {
            return new AuthenticationException("Error parsing the server response", e);
        }
    }

    /**
     * Switches request body to urlencoded(x-www-form-urlencoded) instead of json.
     *
     * @return this
     */
    public ParameterizableRequest<T, AuthenticationException> useUrlencodedBody() {
        this.useUrlencoding = true;
        return this;
    }

    @Override
    public void onFailure(@NonNull Call request, IOException e) {
        postOnFailure(new AuthenticationException("Request failed", new NetworkErrorException(e)));
    }

    @Override
    public ParameterizableRequest<T, AuthenticationException> addHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    @Override
    public AuthorizableRequest<T, AuthenticationException> setBearer(String jwt) {
        addHeader("Authorization", "Bearer " + jwt);
        return this;
    }

    @Override
    public ParameterizableRequest<T, AuthenticationException> addParameters(Map<String, Object> parameters) {
        builder.addAll(parameters);
        return this;
    }

    @Override
    public ParameterizableRequest<T, AuthenticationException> addParameter(String name, Object value) {
        builder.set(name, value);
        return this;
    }

    @Override
    public void call(PACallback<T, AuthenticationException> callback) {
        setCallback(callback);
        try {
            Request request = doBuildRequest();
            client.newCall(request).enqueue(this);
        } catch (RequestBodyBuildException e) {
            callback.onFailure(new AuthenticationException("Error parsing the request body", e));
        }
    }

    protected abstract Request doBuildRequest();
}
