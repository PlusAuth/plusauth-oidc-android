package com.plusauth.android.api.internal;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.plusauth.android.model.Credentials;

import java.lang.reflect.Type;

/**
 * JsonDeserializer implementation for Credentials class.
 */
class CredentialsDeserializer implements JsonDeserializer<Credentials> {

    @Override
    public Credentials deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();

        String accessToken = getString(object, "access_token");
        String type = getString(object, "token_type");
        String idToken = getString(object, "id_token");
        String refreshToken = getString(object, "refresh_token");
        String scope = getString(object, "scope");
        Long expiresInLong = getLong(object, "expires_in");
        Long expiresAtLong = getLong(object, "expires_at");

        if (expiresAtLong == null) {
            expiresAtLong = System.currentTimeMillis() + expiresInLong;
        }

        return new Credentials(idToken, accessToken, type, refreshToken, expiresInLong, expiresAtLong, scope);
    }


    private String getString(JsonObject obj, String claimName) {
        if (!obj.has(claimName)) {
            return null;
        }
        return obj.get(claimName).getAsString();
    }

    private Long getLong(JsonObject obj, String claimName) {
        if (!obj.has(claimName)) {
            return null;
        }
        return obj.get(claimName).getAsLong();
    }
}