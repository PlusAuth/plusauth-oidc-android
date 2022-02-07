package com.plusauth.android.storage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Interface used by CredentialsManager for storing credentials. Allows you
 * to use your custom Storage solution.
 */
public interface Storage {
    void write(@NonNull String name, @Nullable String value);

    void delete(@NonNull String name);

    String read(@NonNull String name);
}
