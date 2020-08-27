package com.plusauth.android.storage;


import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * {@link Storage} that uses SharedPreferences in Private mode(can only be accessed by
 * this application) for persistence.
 */
public class SharedPreferencesStorage implements Storage {

    private static final String SHARED_PREFERENCES_NAME = "com.plusauth.android";

    private final SharedPreferences preferences;

    public SharedPreferencesStorage(@NonNull Context context) {
        preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }


    public void write(@NonNull String name, @Nullable String value) {
        if (value == null) {
            preferences.edit().remove(name).apply();
        } else {
            preferences.edit().putString(name, value).apply();
        }
    }


    @Nullable
    public String read(@NonNull String name) {
        if (!preferences.contains(name)) {
            return null;
        }
        return preferences.getString(name, null);
    }

    public void delete(@NonNull String name) {
        preferences.edit().remove(name).apply();
    }
}
