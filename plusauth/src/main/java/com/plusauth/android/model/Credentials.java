package com.plusauth.android.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.google.gson.annotations.SerializedName;
import com.plusauth.android.jwt.JWT;

import java.util.Date;

/**
 * Data class for holding credentials retrieved from OIDC.
 */
public class Credentials implements Parcelable {

    public static final Creator<Credentials> CREATOR = new Creator<Credentials>() {
        @Override
        public Credentials createFromParcel(Parcel in) {
            return new Credentials(in);
        }

        @Override
        public Credentials[] newArray(int size) {
            return new Credentials[size];
        }
    };
    @SerializedName("access_token")
    private String accessToken;
    @SerializedName("token_type")
    private String type;
    @SerializedName("id_token")
    private String idToken;
    @SerializedName("refresh_token")
    private String refreshToken;
    @SerializedName("scope")
    private String scope;
    @SerializedName("expires_in")
    private Long expiresIn;
    @SerializedName("expires_at")
    private Long expiresAt;

    private transient Date expiresAtDate;
    private transient JWT idTokenJWT;

    public Credentials(@Nullable String idToken, @Nullable String accessToken, @Nullable String type, @Nullable String refreshToken, @Nullable Long expiresAt, @Nullable String scope) {
        this(idToken, accessToken, type, refreshToken, null, expiresAt, scope);
    }

    public Credentials(@Nullable String idToken, @Nullable String accessToken, @Nullable String type, @Nullable String refreshToken, @Nullable Long expiresIn, @Nullable Long expiresAt, @Nullable String scope) {
        this.idToken = idToken;
        this.accessToken = accessToken;
        this.type = type;
        this.refreshToken = refreshToken;
        this.scope = scope;
        this.expiresIn = expiresIn;
        this.expiresAt = expiresAt;

        if (expiresAt != null) {
            this.expiresAtDate = new Date(expiresAt * 1000);
        }
    }

    protected Credentials(Parcel in) {
        accessToken = in.readString();
        type = in.readString();
        idToken = in.readString();
        refreshToken = in.readString();
        scope = in.readString();
        expiresAt = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(accessToken);
        dest.writeString(type);
        dest.writeString(idToken);
        dest.writeString(refreshToken);
        dest.writeString(scope);
        dest.writeLong(expiresAt);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @VisibleForTesting
    long getCurrentTimeInMillis() {
        return System.currentTimeMillis();
    }

    /**
     * Getter for id token.
     *
     * @return id token
     */
    @Nullable
    public String getIdToken() {
        return idToken;
    }

    /**
     * Converts id token to JWT object that can be used to access
     * id token claims.
     *
     * @return id token jwt, returns null if id token is null
     */
    @Nullable
    public JWT idTokenAsJWT() {
        if (idToken == null) return null;

        if (idTokenJWT == null)
            idTokenJWT = JWT.decode(idToken);

        return idTokenJWT;
    }

    /**
     * Getter for access token.
     *
     * @return access token
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Getter for token type.
     *
     * @return type
     */
    public String getType() {
        return type;
    }

    /**
     * Getter for refresh token.
     *
     * @return refresh token
     */
    @Nullable
    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * Getter for scope.
     *
     * @return scope
     */
    public String getScope() {
        return scope;
    }

    /**
     * Getter for expires at.
     *
     * @return expires at
     */
    public Date getExpiresAt() {
        return expiresAtDate;
    }

    /**
     * Checks expires_at/expires_in claims to determine if credentials are expired
     *
     * @return whether credentials are expired
     */
    public boolean isExpired() {
        return expiresAt != null && expiresAtDate.getTime() < getCurrentTimeInMillis();
    }
}
