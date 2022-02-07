package com.plusauth.android.jwt;

import android.text.TextUtils;

import androidx.annotation.VisibleForTesting;

import java.util.Date;

/**
 * Utility class for token validation.
 */
public abstract class TokenValidator {
    public static final long DEFAULT_CLOCK_SKEW = 1000;

    /**
     * Validates token with given options. Uses defaults for currentTime(now) and clockSkew(1s).
     *
     * @param jwt to be validated
     * @param opts validation options
     * @throws InvalidTokenException if token is invalid
     */
    public static void validate(JWT jwt, TokenValidationOptions opts) throws InvalidTokenException {
        Date now = opts.currentTime != null ? opts.currentTime : new Date();
        long clockSkew = opts.clockSkew != null ? opts.clockSkew : DEFAULT_CLOCK_SKEW;

        validateIssuer(jwt, opts.issuer);
        validateAudience(jwt, opts.audience);
        validateExpiresAt(jwt.getExpiresAt(), now, clockSkew);
        validateIssuedAt(jwt.getIssuedAt(), now, clockSkew);
        validateNonce(jwt, opts.nonce);
        validateAuthTime(jwt, opts.maxAge, clockSkew);
    }

    /**
     * Validates issuer of the token.
     *
     * @param jwt to be validated
     * @param issuer required value
     */
    @VisibleForTesting
    static void validateIssuer(JWT jwt, String issuer) {
        if (TextUtils.isEmpty(jwt.getIssuer())) {
            throw new InvalidTokenException("Issuer (iss) claim is not present.");
        }

        if (!jwt.getIssuer().equals(issuer)) {
            throw new InvalidTokenException(String.format("Issuer (iss) claim %s did not match expected %s.", jwt.getIssuer(), issuer));
        }
    }

    /**
     * Validates audience of the token. If there are multiple audiences
     * azp claim of the token will be validated as well.
     *
     * @param jwt to be validated
     * @param audience required value
     */
    @VisibleForTesting
    static void validateAudience(JWT jwt, String audience) {
        if (jwt.getAudience().isEmpty()) {
            throw new InvalidTokenException("Audience (aud) claim is not present.");
        }

        if (!jwt.getAudience().contains(audience)) {
            throw new InvalidTokenException(String.format("Audience (aud) claim %s did not include expected %s.", jwt.getAudience(), audience));
        }

        if (jwt.getAudience().size() > 1) {
            validateAzp(jwt.getClaim("azp").asString(), audience);
        }
    }

    /**
     * Validates azp claim of the token.
     *
     * @param azp to be validated
     * @param audience required value
     */
    @VisibleForTesting
    static void validateAzp(String azp, String audience) {
        if (TextUtils.isEmpty(azp)) {
            throw new InvalidTokenException("Authorized Party (azp) claim must be present if there are mode than one Audience (aud) claim.");
        }

        if (!azp.equals(audience)) {
            throw new InvalidTokenException(String.format("Authorized Party (azp) claim %s did not match expected %s.", azp, audience));
        }

    }

    /**
     * Validates expires_at claim of the token.
     *
     * @param issuedAt to be validated
     * @param now date to be checked against
     * @param clockSkew amount of clock skew
     */
    @VisibleForTesting
    static void validateExpiresAt(Date issuedAt, Date now, long clockSkew) {
        Date lowerLimit = new Date(now.getTime() - clockSkew);

        if (issuedAt == null) {
            throw new InvalidTokenException("Expiration Time (exp) claim is not present.");
        }

        if (lowerLimit.after(issuedAt)) {
            throw new InvalidTokenException(String.format("Expiration Time (exp) claim has expired, Value: %s < Lower limit: %s", issuedAt, lowerLimit));
        }
    }

    /**
     * Validates issued_at claim of the token.
     *
     * @param issuedAt to be validated
     * @param now date to be checked against
     * @param clockSkew amount of clock skew
     */
    @VisibleForTesting
    static void validateIssuedAt(Date issuedAt, Date now, long clockSkew) {
        Date upperLimit = new Date(now.getTime() + clockSkew);
        if (issuedAt == null) {
            throw new InvalidTokenException("Issued At (iat) claim  is not present.");
        }


        if (upperLimit.before(issuedAt)) {
            throw new InvalidTokenException(String.format("Issued At (iat) claim cannot be in the future! Value: %s < Upper Limit: %s", issuedAt, upperLimit));
        }
    }

    /**
     * Validates nonce of the token.
     *
     * @param jwt to be validated
     * @param nonce required value
     */
    @VisibleForTesting
    static void validateNonce(JWT jwt, String nonce) {
        if (nonce != null) {
            String nonceClaim = jwt.getClaim("nonce").asString();
            if (TextUtils.isEmpty(nonceClaim)) {
                throw new InvalidTokenException("Nonce (nonce) claim is not present.");
            }
            if (!nonce.equals(nonceClaim)) {
                throw new InvalidTokenException(String.format("Nonce (nonce) claim  %s did not match expected %s.", nonceClaim, nonce));
            }
        }
    }

    /**
     * Validates auth_time of the token.
     *
     * @param jwt to be validated
     * @param maxAge allowed max age
     * @param clockSkew amount of clock skew
     */
    @VisibleForTesting
    static void validateAuthTime(JWT jwt, Long maxAge, long clockSkew) {
        if (maxAge != null) {
            Date authTime = jwt.getClaim("auth_time").asDate();
            if (authTime == null) {
                throw new InvalidTokenException("Authentication Time (auth_time) claim is not present.");
            }

            Date upperLimitMaxAge = new Date(authTime.getTime() + maxAge + clockSkew);

            if (upperLimitMaxAge.after(authTime)) {
                throw new InvalidTokenException(String.format("Authentication Time (auth_time) is old, too much time has elapsed since the last End-User authentication. Value: %s Upper Limit: %s", authTime, upperLimitMaxAge));
            }
        }
    }
}
