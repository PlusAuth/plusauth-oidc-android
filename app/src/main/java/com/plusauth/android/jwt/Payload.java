package com.plusauth.android.jwt;

import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Data class for JWT payload.
 */
public class Payload {
    /**
     * iss claim, Issuer uri of the token
     */
    final String issuer;
    /**
     * sub claim a.k.a user id
     */
    final String userId;
    /**
     * sub claim, subject of the id token
     */
    final String subject;
    /**
     * exp claim, the date token expires at
     */
    final Date expiresAt;
    /**
     * iat claim, the date token was issued
     */
    final Date issuedAt;
    /**
     * aud claim, list of audiences of token
     */
    final List<String> audience;
    /**
     * immutable map containing all claims of the token
     */
    final Map<String, Claim> claims;

    Payload(String issuer, String userId, Date expiresAt, Date issuedAt, List<String> audience, Map<String, Claim> claims) {
        this.issuer = issuer;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.issuedAt = issuedAt;
        this.audience = audience;
        this.claims = Collections.unmodifiableMap(claims);
        this.subject = userId;
    }

    /**
     * Returns claim of specified name.
     *
     * @param name name of the claim
     * @return claim
     */
    Claim getClaim(String name) {
        Claim claim = this.claims.get(name);

        if (claim != null) return claim;

        return new Claim(null);
    }
}
