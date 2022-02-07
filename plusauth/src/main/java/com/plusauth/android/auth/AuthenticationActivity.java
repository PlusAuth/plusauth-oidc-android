package com.plusauth.android.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.plusauth.android.customtabs.CustomTabsController;
import com.plusauth.android.customtabs.CustomTabsOptions;
import com.plusauth.android.model.Account;

/**
 * Activity used by LoginManager and LogoutManager. Launches given uri in a CustomTab if supported,
 * otherwise a browser. Use the AuthenticationActivity.authenticate method for auto configuration of
 * intent and extras creation.
 *
 */
public class AuthenticationActivity extends Activity {

    static final String EXTRA_AUTHORIZE_URI = "pa.EXTRA_AUTHORIZE_URI";
    static final String EXTRA_CT_OPTIONS = "pa.EXTRA_CT_OPTIONS";
    private static final String EXTRA_INTENT_LAUNCHED = "pa.EXTRA_INTENT_LAUNCHED";

    private boolean intentLaunched;
    private CustomTabsController customTabsController;

    /**
     * Creates, configures and starts AuthenticationActivity.
     *
     * @param context app/local context
     * @param authorizeUri uri to visit
     * @param options custom tabs configuration
     */
    public static void authenticate(@NonNull Context context, @NonNull Uri authorizeUri, @Nullable CustomTabsOptions options) {
        Intent intent = new Intent(context, AuthenticationActivity.class);
        intent.putExtra(AuthenticationActivity.EXTRA_AUTHORIZE_URI, authorizeUri);
        intent.putExtra(AuthenticationActivity.EXTRA_CT_OPTIONS, options);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) {
            data = new Intent();
        }
        deliverAuthenticationResult(data);
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_INTENT_LAUNCHED, intentLaunched);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            intentLaunched = savedInstanceState.getBoolean(EXTRA_INTENT_LAUNCHED, false);
        }
    }

    /**
     * If custom tabs is not launched does so, if was is launched delivers intent data to configured
     * manager.
     */
    @Override
    protected void onResume() {
        super.onResume();
        final Intent authenticationIntent = getIntent();
        if (!intentLaunched && authenticationIntent.getExtras() == null) {
            //Activity was launched in an unexpected way
            finish();
            return;
        } else if (!intentLaunched) {
            intentLaunched = true;
            launchCustomTab();
            return;
        }

        boolean resultMissing = authenticationIntent.getData() == null;
        if (resultMissing) {
            setResult(RESULT_CANCELED);
        }
        deliverAuthenticationResult(authenticationIntent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (customTabsController != null) {
            customTabsController.unbindService();
            customTabsController = null;
        }
    }

    /**
     * Launches AuthorizeUri custom tab with CustomTabOptions.
     */
    private void launchCustomTab() {
        Bundle extras = getIntent().getExtras();
        Uri authorizeUri = extras.getParcelable(EXTRA_AUTHORIZE_URI);

        customTabsController = createCustomTabsController(this);
        customTabsController.setCustomizationOptions(extras.getParcelable(EXTRA_CT_OPTIONS));
        customTabsController.bindService();
        customTabsController.launchUri(authorizeUri);
    }

    @VisibleForTesting
    CustomTabsController createCustomTabsController(@NonNull Context context) {
        return new CustomTabsController(context);
    }

    @VisibleForTesting
    void deliverAuthenticationResult(Intent result) {
        Account.resume(result);
    }

}
