package com.plusauth.android.jwt;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.plusauth.android.crypto.CryptoUtil;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

/**
 * Class for handling Json Web Tokens.
 */
public class JWT implements Parcelable {

    public static final Creator<JWT> CREATOR = new Creator<JWT>() {
        @Override
        public JWT createFromParcel(Parcel in) {
            return JWT.decode(in.readString());
        }

        @Override
        public JWT[] newArray(int size) {
            return new JWT[size];
        }
    };
    public final String token;
    public final Header header;
    public final Payload payload;
    public final String signature;

    public JWT(String token, Header header, Payload payload, String signature) {
        this.token = token;
        this.header = header;
        this.payload = payload;
        this.signature = signature;
    }

    private static Gson getParser() {
        return new GsonBuilder()
                .registerTypeAdapter(Payload.class, new JWTDeserializer())
                .create();
    }

    /**
     * Splits the token string by . delimiter to 3 parts.
     *
     * @param token full jwt token string
     * @return token string split as 3 parts
     */
    private static String[] splitToken(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new JWTDecodeException("Invalid JWT token. Tokens must have exactly 3 parts.");
        }
        return parts;
    }

    private static <T> T parseJson(String json, Type typeOfT) {
        T payload;
        try {
            payload = getParser().fromJson(json, typeOfT);
        } catch (Exception e) {
            throw new JWTDecodeException("The payload or the header is in invalid JSON format.", e);
        }
        return payload;
    }

    /**
     * Decodes and converts the provided token string to JWT
     *
     * @param token token to be decoded
     * @return converted JWT
     */
    public static JWT decode(String token) {
        final String[] parts = splitToken(token);
        Header header = parseJson(CryptoUtil.decodeBase64ToString(parts[0]), Header.class);
        Payload payload = parseJson(CryptoUtil.decodeBase64ToString(parts[1]), Payload.class);
        String signature = parts[2];

        return new JWT(token, header, payload, signature);
    }

    /**
     * iss claim of jwt
     *
     * @return issuer as string
     */
    public String getIssuer() {
        return payload.issuer;
    }

    /**
     * sub claim of jwt
     *
     * @return user id as string
     */
    public String getUserId() {
        return payload.userId;
    }

    /**
     * sub claim of jwt
     *
     * @return subject as string
     */
    public String getSubject() {
        return payload.userId;
    }

    /**
     * exp claim of jwt
     *
     * @return expires at date
     */
    public Date getExpiresAt() {
        return payload.expiresAt;
    }

    /**
     * iat claim of jwt
     *
     * @return issued at date
     */
    public Date getIssuedAt() {
        return payload.issuedAt;
    }

    /**
     * Getter for getting claims from payload of the JWT.
     *
     * @param name name of the claim
     * @return claim from payload
     */
    public Claim getClaim(@NonNull String name) {
        return payload.getClaim(name);
    }

    /**
     * aud claim of jwt
     *
     * @return list of audience entries
     */
    public List<String> getAudience() {
        return payload.audience;
    }

    @Override
    public String toString() {
        return "JWT{" +
                "token='" + token + '\'' +
                ", header=" + header +
                ", payload=" + payload +
                ", signature='" + signature + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(token);
    }

    public boolean isExpired(long clockSkew) {
        long now = new Date().getTime();
        Date past = new Date(now - clockSkew);

        return payload.expiresAt != null && past.after(payload.expiresAt);
    }
}
