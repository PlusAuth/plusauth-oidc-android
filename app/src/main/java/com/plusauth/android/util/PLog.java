package com.plusauth.android.util;

import android.util.Log;

public final class PLog {
    private static boolean enabled = false;

    public static void setEnabled(boolean enabled) {
        PLog.enabled = enabled;
    }

    public static void d(String s) {
        if (enabled)
            Log.d("PLUSAUTH", s);
    }

    public static void e(String s, Throwable e) {
        if (enabled)
            Log.e("PLUSAUTH", s, e);
    }

    public static void e(String s) {
        if (enabled)
            Log.e("PLUSAUTH", s);
    }
}
