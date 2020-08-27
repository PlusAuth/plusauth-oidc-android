package com.plusauth.android.jwt;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JsonDeserializer implementation for Payload
 */
class JWTDeserializer implements JsonDeserializer<Payload> {
    @Override
    public Payload deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject object = json.getAsJsonObject();

        String iss = getAsString(object, "iss");
        String sub = getAsString(object, "sub");
        Date exp = getAsDate(object, "exp");
        Date iat = getAsDate(object, "iat");
        List<String> aud = getAsList(object, "aud");

        //All Claims
        Map<String, Claim> allClaims = new HashMap<>();
        for (Map.Entry<String, JsonElement> e : object.entrySet()) {
            allClaims.put(e.getKey(), new Claim(e.getValue()));
        }

        return new Payload(iss, sub, exp, iat, aud, allClaims);
    }

    private List<String> getAsList(JsonObject obj, String claimName) {
        List<String> list = Collections.emptyList();
        if (obj.has(claimName)) {
            JsonElement arrElement = obj.get(claimName);
            if (arrElement.isJsonArray()) {
                JsonArray jsonArr = arrElement.getAsJsonArray();
                list = new ArrayList<>(jsonArr.size());
                for (int i = 0; i < jsonArr.size(); i++) {
                    list.add(jsonArr.get(i).getAsString());
                }
            } else {
                list = Collections.singletonList(arrElement.getAsString());
            }
        }
        return list;
    }

    private Date getAsDate(JsonObject obj, String claimName) {
        if (!obj.has(claimName)) {
            return null;
        }
        long ms = obj.get(claimName).getAsLong() * 1000;
        return new Date(ms);
    }

    private String getAsString(JsonObject obj, String claimName) {
        if (!obj.has(claimName)) {
            return null;
        }
        return obj.get(claimName).getAsString();
    }
}