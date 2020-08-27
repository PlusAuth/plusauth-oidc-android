package com.plusauth.android.jwt;

import java.util.Date;

/**
 * Data class for holds options for token validation.
 */
public class TokenValidationOptions {
    /**
     * Required issuer of token, a.k.a iss claim
     */
    public final String issuer;
    /**
     * Required audience of token, a.k.a aud claim
     */
    public final String audience;
    /**
     * Date to be checked against for time related claims
     */
    public Date currentTime;
    /**
     * Allowed clock skew for time related claims
     */
    public Long clockSkew;
    /**
     * Required nonce of token
     */
    public String nonce;
    /**
     * Required max age of token, using auth_time claim
     */
    public Long maxAge;

    public TokenValidationOptions(String issuer, String audience) {
        this.issuer = issuer;
        this.audience = audience;
    }
}
