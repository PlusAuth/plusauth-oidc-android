
package com.plusauth.android.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;

/**
 * Data class for holding profile data from /userinfo endpoint.
 */
public class UserProfile implements Serializable {
    @SerializedName("sub")
    private String id;
    @SerializedName("name")
    private String name;
    @SerializedName("nickname")
    private String nickname;
    @SerializedName("picture")
    private String pictureUrl;
    @SerializedName("email")
    private String email;
    @SerializedName("email_verified")
    private boolean emailVerified;
    @SerializedName("given_name")
    private String givenName;
    @SerializedName("family_name")
    private String familyName;
    @SerializedName("phone")
    private String phone;
    @SerializedName("locale")
    private String locale;
    @SerializedName("birthdate")
    private String birthDate;
    @SerializedName("metadata")
    private Map<String, Object> metadata;


    /**
     * Getter for id/sub.
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Getter for name.
     *
     * @return name
     */
    @Nullable
    public String getName() {
        return name;
    }

    /**
     * Getter for nickname.
     *
     * @return nickname
     */
    @Nullable
    public String getNickname() {
        return nickname;
    }

    /**
     * Getter for pictureUrl.
     *
     * @return pictureUrl
     */
    @Nullable
    public String getPictureUrl() {
        return pictureUrl;
    }

    /**
     * Getter for email.
     *
     * @return email
     */
    @Nullable
    public String getEmail() {
        return email;
    }

    /**
     * Getter for emailVerified.
     *
     * @return emailVerified
     */
    @Nullable
    public boolean isEmailVerified() {
        return emailVerified;
    }

    /**
     * Getter for givenName.
     *
     * @return givenName
     */
    @Nullable
    public String getGivenName() {
        return givenName;
    }

    /**
     * Getter for familyName.
     *
     * @return familyName
     */
    @Nullable
    public String getFamilyName() {
        return familyName;
    }

    /**
     * Getter for phone.
     *
     * @return phone
     */
    @Nullable
    public String getPhone() {
        return phone;
    }

    /**
     * Getter for locale.
     *
     * @return locale
     */
    @Nullable
    public String getLocale() {
        return locale;
    }

    /**
     * Getter for birthDate.
     *
     * @return birthDate
     */
    @Nullable
    public String getBirthDate() {
        return birthDate;
    }

    /**
     * Getter for metadata.
     *
     * @return metadata
     */
    @Nullable
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", nickname='" + nickname + '\'' +
                ", pictureUrl='" + pictureUrl + '\'' +
                ", email='" + email + '\'' +
                ", emailVerified=" + emailVerified +
                ", givenName='" + givenName + '\'' +
                ", familyName='" + familyName + '\'' +
                ", phone='" + phone + '\'' +
                ", locale='" + locale + '\'' +
                ", birthDate='" + birthDate + '\'' +
                ", metadata=" + metadata +
                '}';
    }
}
