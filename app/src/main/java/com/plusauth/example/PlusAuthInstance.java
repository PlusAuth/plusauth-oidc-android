package com.plusauth.example;

import android.content.Context;

import com.plusauth.android.OIDC;
import com.plusauth.android.OIDCBuilder;

public class PlusAuthInstance {
    private static OIDC plusAuth;

    public static OIDC get(Context context) {
        if (plusAuth == null) {
            plusAuth = new OIDCBuilder(context, "<YOUR_CLIENT_ID>", "https://<YOUR_TENANT_ID>.plusauth.com")
                    .setLoggingEnabled(true)
                    .build();
        }

         return plusAuth;
    }

}
