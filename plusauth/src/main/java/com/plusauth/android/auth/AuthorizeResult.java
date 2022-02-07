
package com.plusauth.android.auth;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;

/**
 * Model for dealing with results from AuthorizationActivity.
 */
public class AuthorizeResult {

    private final int resultCode;
    private final Intent intent;

    public AuthorizeResult(@Nullable Intent intent) {
        this.intent = intent;
        this.resultCode = getIntentData() != null ? Activity.RESULT_OK : Activity.RESULT_CANCELED;
    }

    /**
     * Checks whether activity was canceled or has data
     *
     * @return whether result was canceled
     */
    public boolean isCanceled() {
        return resultCode == Activity.RESULT_CANCELED && intent != null && getIntentData() == null;
    }

    @Nullable
    public Uri getIntentData() {
        return intent == null ? null : intent.getData();
    }

}
