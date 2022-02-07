package com.plusauth.android.api.internal;

import com.google.gson.Gson;
import com.plusauth.android.api.RequestBodyBuildException;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Convenience class for converting objects to JSON encoded request bodies.
 */
public abstract class JsonRequestBodyBuilder {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /**
     * Converts given object to {@link RequestBody}
     *
     * @param obj Object to be converted to request body
     * @param gson Gson instance for serialization
     * @return {@link RequestBody} type of provided object
     * @throws RequestBodyBuildException if obj cannot be converted to json
     */
    public static RequestBody createBody(Object obj, Gson gson) throws RequestBodyBuildException {
        try {
            return RequestBody.create(JSON, gson.toJson(obj));
        } catch (Exception e) {
            throw new RequestBodyBuildException("Failed to convert " + obj.getClass().getName() + " to JSON", e);
        }
    }
}
