package com.plusauth.android.jwt;

import com.google.gson.annotations.SerializedName;

/**
 * Data class for JWT header
 */
public class Header {
    /**
     * 'alg' claim of the JWT header
     */
    @SerializedName("alg")
    public final String algorithm;

    /**
     * 'typ' claim of the JWT header
     */
    @SerializedName("typ")
    public final String type;

    public Header(String alg, String typ) {
        this.algorithm = alg;
        this.type = typ;
    }
}
