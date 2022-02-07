package com.plusauth.android;

import android.content.Context;

public class PlusAuthInstance {
    private static OIDC plusAuth;

    public static OIDC get(Context context) {
        if (plusAuth == null) {
            //plusAuth = new OIDCBuilder(context, "<YOUR_CLIENT_ID>", "https://<YOUR_TENANT_ID>.plusauth.com")
            plusAuth = new OIDCBuilder(context, "dezwrNoatwzUJO4FtsOFIWyDQbTMex3dfXlOaGQrCHPCcRFp", "https://starters.plusauth.com")
                    .setLoggingEnabled(true)
                    .build();
        }

         return plusAuth;
    }

}
