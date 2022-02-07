package com.plusauth.android;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.plusauth.android.auth.exceptions.AuthenticationException;
import com.plusauth.android.auth.login.LoginRequest;
import com.plusauth.android.auth.logout.LogoutRequest;
import com.plusauth.android.model.Credentials;
import com.plusauth.android.model.UserProfile;
import com.plusauth.android.util.AuthenticationCallback;
import com.plusauth.android.util.PACallback;
import com.plusauth.android.util.VoidCallback;

public class MainActivity extends AppCompatActivity {

    Button buttonLogin, buttonLogout;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonLogin = findViewById(R.id.button_login);
        buttonLogout = findViewById(R.id.button_logout);

        OIDC plusAuth = PlusAuthInstance.get(this);

        if(plusAuth.getCredentialsManager().hasValidCredentials()) {
            plusAuth.getApi().userInfo().call(new PACallback<UserProfile, AuthenticationException>() {
                @Override
                public void onSuccess(UserProfile userProfile) {
                    // Show Profile and Logout buttons , hide Login Button
                    runOnUiThread(() -> configureViews(true));
                }
                @Override
                public void onFailure(AuthenticationException e) {
                    // Show Login button, hide Profile and Logout buttons
                    runOnUiThread(() -> configureViews(false));
                }
            });
        } else {
            // Show Login button, hide Profile and Logout buttons
            runOnUiThread(() -> configureViews(false));
        }

        buttonLogin.setOnClickListener(v -> {
            // Trigger Login to PlusAuth
            plusAuth.login(this, new LoginRequest().setScope("openid offline_access profile email"), new AuthenticationCallback() {
                @Override
                public void onSuccess(Credentials credentials) {
                    Log.e(TAG, "Access Token: " + credentials.getAccessToken());
                    runOnUiThread(() -> configureViews(true));
                }
                @Override
                public void onFailure(AuthenticationException e) {
                    Log.e(TAG, "Login failed", e);
                }
            });
        });

        buttonLogout.setOnClickListener(v -> {
            plusAuth.logout(this, new LogoutRequest(), new VoidCallback() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.e(TAG, "Logout Successful");
                    runOnUiThread(() -> configureViews(false));
                }
                @Override
                public void onFailure(AuthenticationException e) {
                    Log.e(TAG, "Logout failed", e);
                }
            });
        });
    }

    private void configureViews(Boolean isLoggedIn) {
        buttonLogin.setVisibility(isLoggedIn ? View.GONE : View.VISIBLE);
        buttonLogout.setVisibility(isLoggedIn ? View.VISIBLE : View.GONE);
    }
}