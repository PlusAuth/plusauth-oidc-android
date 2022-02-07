package com.plusauth.android.util;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for dealing with uris.
 */
public abstract class UriHelper {


    /**
     * Creates oidc callback uri from given packageName.
     *
     * @param packageName name of the package
     * @return callback uri for this app
     */
    public static String getCallbackUri(@NonNull String packageName) {
        return String.format("%s:/callback", packageName);
    }

    /**
     * Parses uri and returns extracted query params as map.
     *
     * @param uri uri to be parsed
     * @return query params of uri
     */
    @NonNull
    public static Map<String, String> getQueryParamsFromUri(@Nullable Uri uri) {
        if (uri == null) {
            return Collections.emptyMap();
        }
        return asMap(uri.getQuery() != null ? uri.getQuery() : uri.getFragment());
    }

    /**
     * Converts query string to map of query params.
     *
     * @param queryString full query as string
     * @return map created from query
     */
    private static Map<String, String> asMap(@Nullable String queryString) {
        if (queryString == null) {
            return new HashMap<>();
        }
        final String[] entries = queryString.length() > 0 ? queryString.split("&") : new String[]{};
        Map<String, String> values = new HashMap<>(entries.length);
        for (String entry : entries) {
            final String[] value = entry.split("=");
            if (value.length == 2) {
                values.put(value[0], value[1]);
            }
        }
        return values;
    }
}