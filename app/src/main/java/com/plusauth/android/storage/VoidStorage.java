package com.plusauth.android.storage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Convenience Storage class that does not store anything.
 */
public class VoidStorage implements Storage {
    @Override
    public void write(@NonNull String name, @Nullable String value) {

    }

    @Override
    public void delete(@NonNull String name) {

    }

    @Override
    public String read(@NonNull String name) {
        return null;
    }
}
