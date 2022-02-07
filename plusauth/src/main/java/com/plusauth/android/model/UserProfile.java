
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
    @SerializedName("username")
    private String username;
    @SerializedName("gender")
    private String gender;
    @SerializedName("picture")
    private String pictureUrl;
    @SerializedName("email")
    private String email;
    @SerializedName("email_verified")
    private boolean emailVerified;
    @SerializedName("given_name")
    private String givenName;
    @SerializedName("middle_name")
    private String middleName;
    @SerializedName("family_name")
    private String familyName;
    @SerializedName("phone")
    private String phone;
    @SerializedName("locale")
    private String locale;
    @SerializedName("nickname")
    private String nickname;
    @SerializedName("birthdate")
    private String birthDate;
    @SerializedName("preferred_username")
    private String preferredUsername;
    @SerializedName("website")
    private String website;
    @SerializedName("zoneinfo")
    private String zoneinfo;
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
     * Getter for username.
     *
     * @return username
     */
    @Nullable
    public String getUsername() {
        return username;
    }

    /**
     * Getter for gender.
     *
     * @return gender
     */
    @Nullable
    public String getGender() {
        return gender;
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
     * Getter for middleName.
     *
     * @return middleName
     */
    @Nullable
    public String getMiddleName() {
        return middleName;
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
     * Getter for nickname.
     *
     * @return nickname
     */
    @Nullable
    public String getNickname() {
        return nickname;
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
     * Getter for preferredUsername.
     *
     * @return preferredUsername
     */
    @Nullable
    public String getPreferredUsername() {
        return preferredUsername;
    }

    /**
     * Getter for website.
     *
     * @return website
     */
    @Nullable
    public String getWebsite() {
        return website;
    }

    /**
     * Getter for zoneinfo.
     *
     * @return zoneinfo
     */
    @Nullable
    public String getZoneinfo() {
        return zoneinfo;
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
                ", username='" + username + '\'' +
                ", gender='" + gender + '\'' +
                ", pictureUrl='" + pictureUrl + '\'' +
                ", email='" + email + '\'' +
                ", emailVerified=" + emailVerified +
                ", givenName='" + givenName + '\'' +
                ", middleName='" + middleName + '\'' +
                ", familyName='" + familyName + '\'' +
                ", phone='" + phone + '\'' +
                ", locale='" + locale + '\'' +
                ", nickname='" + nickname + '\'' +
                ", birthDate='" + birthDate + '\'' +
                ", preferredUsername='" + preferredUsername + '\'' +
                ", website='" + website + '\'' +
                ", zoneinfo='" + zoneinfo + '\'' +
                ", metadata=" + metadata +
                '}';
    }
}
