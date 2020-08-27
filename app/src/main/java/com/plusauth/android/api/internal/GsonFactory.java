package com.plusauth.android.api.internal;

import androidx.annotation.VisibleForTesting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.plusauth.android.model.Credentials;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Factory class for creating Gson instances with default options.
 */
public abstract class GsonFactory {

    static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static Gson build() {
        return new GsonBuilder()
                .registerTypeAdapter(Credentials.class, new CredentialsDeserializer())
                .setDateFormat(DATE_FORMAT)
                .create();
    }

    @VisibleForTesting
    static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        return sdf.format(date);
    }
}
