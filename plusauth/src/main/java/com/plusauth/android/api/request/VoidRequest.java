package com.plusauth.android.api.request;

import com.google.gson.Gson;
import com.plusauth.android.BaseException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Concrete implementation of {@link BaseRequest} class.
 * Response won't be parsed.
 */
class VoidRequest extends BaseRequest<Void> implements Callback {

    private final String httpMethod;

    public VoidRequest(HttpUrl url, OkHttpClient client, Gson gson, String httpMethod) {
        super(url, client, gson, gson.getAdapter(Void.class));
        this.httpMethod = httpMethod;
    }

    @Override
    protected Request doBuildRequest() {
        RequestBody body = buildBody();
        return newBuilder()
                .method(httpMethod, body)
                .build();
    }

    @Override
    public Void callSync() throws BaseException {
        Request request = doBuildRequest();

        Response response;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            throw new BaseException("Failed to execute request to " + url.toString(), e);
        }

        if (!response.isSuccessful()) {
            throw parseUnsuccessfulResponse(response);
        }
        return null;
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        if (!response.isSuccessful()) {
            postOnFailure(parseUnsuccessfulResponse(response));
            return;
        }

        postOnSuccess(null);
    }
}
