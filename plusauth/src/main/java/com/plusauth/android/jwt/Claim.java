package com.plusauth.android.jwt;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Convenience class for dealing with claim entries of JWTs.
 */
public class Claim {
    private final JsonElement value;

    Claim(JsonElement value) {
        this.value = value;
    }

    /**
     * Converts claim to Boolean and returns it. Will return null
     * if value is null.
     *
     * @return claim as Boolean
     */
    @Nullable
    public Boolean asBoolean() {
        if (value == null) return null;

        return value.getAsBoolean();
    }

    /**
     * Converts claim to Integer and returns it. Will return null
     * if value is null.
     *
     * @return claim as Integer
     */
    @Nullable
    public Integer asInt() {
        if (value == null) return null;
        return value.getAsInt();
    }

    /**
     * Converts claim to Long and returns it. Will return null
     * if value is null.
     *
     * @return claim as Long
     */
    @Nullable
    public Long asLong() {
        if (value == null) return null;
        return value.getAsLong();
    }

    /**
     * Converts claim to Double and returns it. Will return null
     * if value is null.
     *
     * @return claim as Double
     */
    @Nullable
    public Double asDouble() {
        if (value == null) return null;
        return value.getAsDouble();
    }

    /**
     * Converts claim to String and returns it. Will return null
     * if value is null.
     *
     * @return claim as String
     */
    @Nullable
    public String asString() {
        if (value == null) return null;
        return value.getAsString();
    }

    /**
     * Converts claim to Date and returns it. Will return null
     * if value is null.
     *
     * @return claim as Date
     */
    @Nullable
    public Date asDate() {
        if (value == null) return null;
        long ms = value.getAsLong() * 1000;
        return new Date(ms);
    }

    /**
     * Converts claim to array and returns it. Will return null
     * if value is null.
     *
     * @param <T> type of array
     * @param tClazz class that array holds
     * @return claim as array
     */
    @Nullable
    public <T> T[] asArray(Class<T> tClazz) throws JWTDecodeException {
        if (value == null) return null;
        try {
            if (!value.isJsonArray() || value.isJsonNull()) {
                return (T[]) Array.newInstance(tClazz, 0);
            }
            Gson gson = new Gson();
            JsonArray jsonArr = value.getAsJsonArray();
            T[] arr = (T[]) Array.newInstance(tClazz, jsonArr.size());
            for (int i = 0; i < jsonArr.size(); i++) {
                arr[i] = gson.fromJson(jsonArr.get(i), tClazz);
            }
            return arr;
        } catch (JsonSyntaxException e) {
            throw new JWTDecodeException("Failed to decode claim as array", e);
        }
    }

    /**
     * Converts claim to List and returns it. Will return null
     * if value is null.
     *
     * @param <T> type of list
     * @param tClazz class that list holds
     * @return claim as List
     */
    @Nullable
    public <T> List<T> asList(Class<T> tClazz) throws JWTDecodeException {
        if (value == null) return null;
        try {
            if (!value.isJsonArray() || value.isJsonNull()) {
                return new ArrayList<>();
            }
            Gson gson = new Gson();
            JsonArray jsonArr = value.getAsJsonArray();
            List<T> list = new ArrayList<>();
            for (int i = 0; i < jsonArr.size(); i++) {
                list.add(gson.fromJson(jsonArr.get(i), tClazz));
            }
            return list;
        } catch (JsonSyntaxException e) {
            throw new JWTDecodeException("Failed to decode claim as list", e);
        }
    }

    /**
     * Converts claim to T and returns it. Will return null
     * if value is null.
     *
     * @param <T> type of object
     * @param tClazz class that object holds
     * @return claim as T
     */
    @Nullable
    public <T> T asObject(Class<T> tClazz) throws JWTDecodeException {
        if (value == null) return null;
        try {
            if (value.isJsonNull()) {
                return null;
            }
            return new Gson().fromJson(value, tClazz);
        } catch (JsonSyntaxException e) {
            throw new JWTDecodeException("Failed to decode claim as " + tClazz.getSimpleName(), e);
        }
    }
}
