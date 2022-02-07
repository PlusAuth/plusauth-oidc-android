package com.plusauth.android.api.internal;

import com.plusauth.android.api.RequestBodyBuildException;

import java.util.Map;

import okhttp3.FormBody;
import okhttp3.RequestBody;

/**
 * Convenience class for converting objects to Urlencoded request bodies.
 */
public abstract class UrlencodedRequestBodyBuilder {

    /**
     * Converts given params map to {@link RequestBody}
     *
     * @param params Params to be converted to request body
     * @return {@link RequestBody} type of provided params
     * @throws RequestBodyBuildException if params are failed to convert to form body
     */
    public static RequestBody createBody(Map<String, Object> params) throws RequestBodyBuildException {
        try {
            FormBody.Builder builder = new FormBody.Builder();

            for (Map.Entry<String, Object> param : params.entrySet()) {
                builder.addEncoded(param.getKey(), String.valueOf(param.getValue()));
            }

            return builder.build();

        } catch (Exception e) {
            throw new RequestBodyBuildException("Failed to convert " + params + " to Formbody", e);
        }
    }
}
