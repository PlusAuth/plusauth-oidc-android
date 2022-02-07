package com.plusauth.android.customtabs;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsServiceConnection;
import androidx.browser.customtabs.CustomTabsSession;

import com.plusauth.android.util.PLog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Class for dealing with CustomTabsServiceConnection in an easier manner.
 */
public class CustomTabsController extends CustomTabsServiceConnection {

    private static final long MAX_WAIT_TIME_SECONDS = 1;
    private static final String ACTION_CUSTOM_TABS_CONNECTION = "androidx.browser.customtabs.action.CustomTabsService";
    private static final String CHROME_STABLE = "com.android.chrome";
    private static final String CHROME_SYSTEM = "com.google.android.apps.chrome";
    private static final String CHROME_BETA = "com.android.chrome.beta";

    private final WeakReference<Context> context;
    private final AtomicReference<CustomTabsSession> session;
    private final CountDownLatch sessionLatch;
    private final String preferredPackage;

    @Nullable
    private CustomTabsOptions customTabsOptions;
    private boolean isBound;

    @VisibleForTesting
    CustomTabsController(@NonNull Context context, @Nullable String browserPackage) {
        this.context = new WeakReference<>(context);
        this.session = new AtomicReference<>();
        this.sessionLatch = new CountDownLatch(1);
        this.preferredPackage = browserPackage;
    }

    public CustomTabsController(@NonNull Context context) {
        this(context, getBestBrowserPackage(context));
    }

    @VisibleForTesting
    void clearContext() {
        this.context.clear();
    }

    /**
     * Sets custom tab customization.
     *
     * @param options options for custom tab appearance
     */
    public void setCustomizationOptions(@Nullable CustomTabsOptions options) {
        this.customTabsOptions = options;
    }

    @VisibleForTesting
    CustomTabsOptions getCustomizationOptions() {
        return this.customTabsOptions;
    }

    @Override
    public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient customTabsClient) {
        if (customTabsClient == null) {
            return;
        }
        PLog.d("CustomTabs Service connected");
        customTabsClient.warmup(0L);
        session.set(customTabsClient.newSession(null));
        sessionLatch.countDown();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        PLog.d("CustomTabs Service disconnected");
        session.set(null);
    }

    /**
     * Attempts to bind custom tab service to context.
     */
    public void bindService() {
        PLog.d("Binding custom tab service");
        Context context = this.context.get();
        isBound = false;
        if (context != null && preferredPackage != null) {
            isBound = CustomTabsClient.bindCustomTabsService(context, preferredPackage, this);
        }
        PLog.d("Bind request result: " + isBound);
    }

    /**
     * Attempts to unbind context from custom tab service.
     */
    public void unbindService() {
        PLog.d("Unbinding custom tab service");
        Context context = this.context.get();
        if (isBound && context != null) {
            context.unbindService(this);
            isBound = false;
        }
    }

    /**
     * Tries to launch provided uri in a custom tab, if not available will try to use
     * a browser.
     *
     * @param uri uri to be launched
     */
    public void launchUri(@NonNull final Uri uri) {
        final Context context = this.context.get();
        if (context == null) {
            PLog.d("Custom Tab Context was no longer valid.");
            return;
        }

        if (customTabsOptions == null) {
            customTabsOptions = CustomTabsOptions.newBuilder().build();
        }

        new Thread(() -> {
            boolean available = false;
            try {
                available = sessionLatch.await(preferredPackage == null ? 0 : MAX_WAIT_TIME_SECONDS, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
            }
            PLog.d("Launching URI. Custom Tabs available: " + available);

            final Intent intent = customTabsOptions.toIntent(context, session.get());
            intent.setData(uri);
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException ex) {
                PLog.e("Could not find any Browser application installed in this device to handle the intent.");
            }
        }).start();
    }

    /**
     * Tries to find an available browser that can be used for custom tabs or plain viewing
     * of the auth page.
     *
     * @param context app/local context
     * @return browser package id
     */
    @Nullable
    static String getBestBrowserPackage(@NonNull Context context) {
        PackageManager pm = context.getPackageManager();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com"));
        ResolveInfo webHandler = pm.resolveActivity(browserIntent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PackageManager.MATCH_ALL : PackageManager.MATCH_DEFAULT_ONLY);
        String defaultBrowser = null;
        if (webHandler != null) {
            defaultBrowser = webHandler.activityInfo.packageName;
        }

        List<ResolveInfo> resolvedActivityList = pm.queryIntentActivities(browserIntent, 0);
        List<String> customTabsBrowsers = new ArrayList<>();
        for (ResolveInfo info : resolvedActivityList) {
            Intent serviceIntent = new Intent();
            serviceIntent.setAction(ACTION_CUSTOM_TABS_CONNECTION);
            serviceIntent.setPackage(info.activityInfo.packageName);
            if (pm.resolveService(serviceIntent, 0) != null) {
                customTabsBrowsers.add(info.activityInfo.packageName);
            }
        }
        if (customTabsBrowsers.contains(defaultBrowser)) {
            return defaultBrowser;
        } else if (customTabsBrowsers.contains(CHROME_STABLE)) {
            return CHROME_STABLE;
        } else if (customTabsBrowsers.contains(CHROME_SYSTEM)) {
            return CHROME_SYSTEM;
        } else if (customTabsBrowsers.contains(CHROME_BETA)) {
            return CHROME_BETA;
        }  else if (!customTabsBrowsers.isEmpty()) {
            return customTabsBrowsers.get(0);
        } else {
            return null;
        }
    }
}