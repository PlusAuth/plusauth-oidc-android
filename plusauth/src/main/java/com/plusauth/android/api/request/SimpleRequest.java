package com.plusauth.android.api.request;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.plusauth.android.BaseException;
import com.plusauth.android.api.NetworkErrorException;
import com.plusauth.android.api.RequestBodyBuildException;
import com.plusauth.android.auth.exceptions.AuthenticationException;

import java.io.IOException;
import java.io.Reader;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;


/**
 * Concrete implementation of {@link BaseRequest} class.
 *
 * @param <T> type that will be returned on successful responses
 */
public class SimpleRequest<T> extends BaseRequest<T> implements ParameterizableRequest<T, AuthenticationException>, Callback {

    private final String method;

    public SimpleRequest(HttpUrl url, OkHttpClient client, Gson gson, String httpMethod, TypeToken<T> typeToken) {
        super(url, client, gson, gson.getAdapter(typeToken));
        this.method = httpMethod;
    }

    public SimpleRequest(HttpUrl url, OkHttpClient client, Gson gson, String httpMethod, Class<T> clazz) {
        super(url, client, gson, gson.getAdapter(clazz));
        this.method = httpMethod;
    }

    public SimpleRequest(HttpUrl url, OkHttpClient client, Gson gson, String httpMethod) {
        super(url, client, gson, gson.getAdapter(new TypeToken<T>() {
        }));
        this.method = httpMethod;
    }

    @Override
    protected Request doBuildRequest() throws RequestBodyBuildException {
        boolean sendBody = method.equals("HEAD") || method.equals("GET");
        return newBuilder()
                .method(method, sendBody ? null : buildBody())
                .build();
    }

    @Override
    public T callSync() throws BaseException {
        Request request = doBuildRequest();

        Response response;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            throw new AuthenticationException("Request failed", new NetworkErrorException(e));
        }

        if (!response.isSuccessful()) {
            throw parseUnsuccessfulResponse(response);
        }

        try (ResponseBody body = response.body()) {
            Reader charStream = body.charStream();
            return getAdapter().fromJson(charStream);
        } catch (IOException e) {
            throw new AuthenticationException("Failed to parse response to request to " + url, e);
        }
    }

    @Override
    public void onResponse(@NonNull Call call, Response response) {
        if (!response.isSuccessful()) {
            postOnFailure(parseUnsuccessfulResponse(response));
            return;
        }


        try (ResponseBody body = response.body()) {
            Reader charStream = body.charStream();
            T payload = getAdapter().fromJson(charStream);
            postOnSuccess(payload);
        } catch (IOException e) {
            postOnFailure(new AuthenticationException("Failed to parse response to request to " + url, e));
        }
    }
}
