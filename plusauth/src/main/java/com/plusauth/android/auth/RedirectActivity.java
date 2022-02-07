package com.plusauth.android.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Activity that will be launched when user visits {applicationId}/callback.
 * Reroutes intent data to AuthenticationActivity.
 */
public class RedirectActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        Intent intent = new Intent(this, AuthenticationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (getIntent() != null) {
            intent.setData(getIntent().getData());
        }
        startActivity(intent);
        finish();
    }

}
