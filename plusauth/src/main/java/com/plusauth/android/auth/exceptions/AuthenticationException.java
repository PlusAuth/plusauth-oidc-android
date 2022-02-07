package com.plusauth.android.auth.exceptions;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.plusauth.android.BaseException;

import java.util.HashMap;
import java.util.Map;

/**
 * General exception class for Api related authentication errors.
 */
public class AuthenticationException extends BaseException {

    public static final String EMPTY_RESPONSE_BODY_DESCRIPTION = "Empty response body";
    public static final String EMPTY_BODY_ERROR = "pa.empty_body";
    public static final String NON_JSON_ERROR = "pa.plain";
    public static final String UNKNOWN_ERROR = "pa.internal.unknown";
    private static final String DESCRIPTION_KEY = "error";
    private static final String ERROR_DESCRIPTION_KEY = "error_description";

    private static final String DEFAULT_MESSAGE = "An unknown error occurred during authentication.";


    private String code;
    private String description;
    private int statusCode;
    private Map<String, Object> values;

    public AuthenticationException(String code, String description) {
        this(DEFAULT_MESSAGE);
        this.code = code;
        this.description = description;
    }

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Exception exception) {
        super(message, exception);
    }

    /**
     * Constructor for wrapping http error.
     *
     * @param payload response body
     * @param statusCode response status code
     */
    public AuthenticationException(String payload, int statusCode) {
        this(DEFAULT_MESSAGE);
        this.code = payload != null ? NON_JSON_ERROR : EMPTY_BODY_ERROR;
        this.description = payload != null ? payload : EMPTY_RESPONSE_BODY_DESCRIPTION;
        this.statusCode = statusCode;
    }

    public AuthenticationException(Map<String, Object> values) {
        this(DEFAULT_MESSAGE);
        this.values = new HashMap<>(values);

        if (!this.values.containsKey(DESCRIPTION_KEY)) {
            this.description = (String) this.values.get(ERROR_DESCRIPTION_KEY);
            return;
        }

        Object description = this.values.get(DESCRIPTION_KEY);
        if (description instanceof String) {
            this.description = (String) description;
        }
    }

    /**
     * Error code of exception
     *
     * @return code
     */
    public String getCode() {
        return code != null ? code : UNKNOWN_ERROR;
    }

    /**
     * Http code for exception
     *
     * @return code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Returns error description from the request or {@literal AuthenticationException.DEFAULT_MESSAGE}
     * as default.
     *
     * @return description
     */
    public String getDescription() {
        if (!TextUtils.isEmpty(description)) {
            return description;
        }

        return DEFAULT_MESSAGE;
    }

    /**
     * If you want a specific value from the error object you can use this method to do so.
     *
     * @param key parameter name of desired value
     * @return value
     */
    @Nullable
    public Object getValue(String key) {
        if (values == null) {
            return null;
        }
        return values.get(key);
    }

    @Override
    public String toString() {
        return "AuthenticationException{" +
                "code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", statusCode=" + statusCode +
                ", values=" + values +
                '}';
    }
}