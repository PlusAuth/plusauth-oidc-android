package com.plusauth.android.util;

public interface PACallback<P, E> {
    void onSuccess(P payload);
    void onFailure(E error);
}
