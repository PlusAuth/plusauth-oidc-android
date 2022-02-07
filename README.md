# PlusAuth OpenID Connect Library for Android
[![MIT license](https://img.shields.io/badge/License-MIT-blue.svg)](https://github.com/PlusAuth/plusauth-oidc-android/blob/master/LICENSE) ![Version](https://img.shields.io/badge/version-1.0.0-green)  ![Android Api](https://img.shields.io/badge/android-21%2B-green) [![GitHub issues](https://img.shields.io/github/issues/Naereen/StrapDown.js.svg)](https://github.com/PlusAuth/plusauth-oidc-android/issues) ![Vulnerability](https://img.shields.io/snyk/vulnerabilities/github/PlusAuth/plusauth-oidc-android?style=flat-square)

Android OpenId Connect library that can be used with any OIDC provider for easy to use authentication. We value developer time and effort, so we developed a dead simple zero config library that any one could easily integrate in a minute, while allowing veterans of authentication to customize everything to their needs.

## Table of Contents

- [Requirements](#Requirements)
- [Installation](#Installation)
- [OIDC Provider Configuration](#OIDC-provider-configuration)
- [Configuration](#Configuration)
- [Login](#Login)
- [Logout](#Logout)
- [Using the Tokens](#Using-the-Tokens)
  - [Get User Info](#Get-user-info)
  - [Exchange Auth Token for Credentials](#Exchange-auth-token-for-credentials)
  - [Renew Credential with a Refresh Token](#Renew-credential-with-a-refresh-token)
  - [Revoke a Token](#Revoke-a-Token)
- [Advanced Usage](#Advanced-usage)
  - [Storage](#Storage)
  - [Encryption](#Encryption)
- [Example App](#Example-app)
- [Acknowledgements](#Acknowledgements)
- [License](#License)



## Requirements

PlusAuth OpenID Connect Library for Android supports Android Apis starting from 16(Jelly Bean) and above.

## Installation

Add the library dependency to your `build.gradle` file:

```gradle
implementation 'com.plusauth.android:oidc:1.0.0'
```

This library uses and requires Java 8 support. Check out [Android Documentation](https://developer.android.com/studio/write/java8-support.html) to learn more.

To enable, add the following following to your `build.gradle` file

```gradle
android {
    ...
    ...
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    // For Kotlin projects
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
```
### OIDC Provider Configuration


To use the library you must have a OIDC provider account. In this part we will use PlusAuth as an example.


1. Create a Plusauth account and a tenant at [PlusAuth Dashboard](https://dashboard.plusauth.com)
2. Navigate to `Clients` tab and create a client of type `Native Application`.
3. Go to details page of the client that you've just created and set the following fields as:

- Redirect Uris: `${your-application-id}:/callback`
- Post Logout Redirect Uris: `${your-application-id}:/callback`


Done! Note your 'Client Id' and 'domain' for library configuration later.

## Configuration

The entry point of the library is the OIDC object. OIDC object can be created by using the OIDCBuilder.

```java
OIDC oidc = new OIDCBuilder(
		context,
		"your-client-id",
		"your-oidc-domain")
    .build();
```


## Login

By default library uses Authorization Code + [PKCE](https://tools.ietf.org/html/rfc7636) flow for maximum security.
Library wraps all steps of auth flow in to a single call:

```java
oidc.login(context, new LoginRequest(), new AuthenticationCallback() {
        @Override
        public void onSuccess(Credentials credentials) {
        	runOnUiThread(()->{
            	// do ui related work
            });
        }

        @Override
        public void onFailure(AuthenticationException e) {
          Log.e("TAG", "Login failed", e);
        }

	});
```

OIDC will open a browser custom tab(if supported) or browser page showing them your OIDC login page and users will be returned back to the app after they complete the login.

After successful response credentials will be locally stored to skip the OIDC flow at next login.
If there are valid credentials in Storage they will be returned instead, skipping OIDC exchange.


This call is asychronous and requires a callback. If you want to do ui related work in callback you must use Activity.runOnUiThread to switch back to main thread since login request is executed in a seperate thread.

## Logout

Logout call is similar to login:

```java
oidc.logout(this, new LogoutRequest(), new VoidCallback() {
    @Override
    public void onSuccess(Void aVoid) {
        runOnUiThread(()->{
            // do ui related work
        });
    }

    @Override
    public void onFailure(AuthenticationException e) {
        Log.e("TAG", "Logout failed", e);
});
```

OIDC will momenrarily open a browser custom tab(if supported) or page of OIDC logout page and users will be immediately returned back to the app. This clears users browser session. 

After successful response credentials will be locally removed. This completes the two step logout process.
If there aren't valid credentials in Storage request will fail.

If you have them, you can revoke your refresh tokens using the [Revoke tokens](#Revoking-a-Token) method from Api.


This call is asychronous and requires a callback. If you want to do ui related work in callback you must use Activity.runOnUiThread to switch back to main thread since login request is executed in a seperate thread.

## Api

Api class wraps common OIDC calls and provides easy to use interface. Requests can be called both asynchronously or synchronously.
No arg method overloads uses provided storage instance
to get required tokens.

Obtain api instance:

```java
Api api = oidc.getApi();
```

If you create the api instance manually, make sure to set credentials manager so the no arg overloads can be used, otherwise you will get null pointer errors(also could occur if storage does not have credentials).

### Get User Info

Get User Profile information using [/userinfo](https://openid.net/specs/openid-connect-core-1_0.html#UserInfo) endpoint:

```java
api.userInfo().call(new PACallback<UserProfile, AuthenticationException>() {
    @Override
    public void onSuccess(UserProfile userProfile) {
        runOnUiThread(() -> {
            // do ui related work
        });
    }

    @Override
    public void onFailure(AuthenticationException e) {
        Log.e("TAG", "Could not get profile", e);
    }
});
```
### Exchange auth token for credentials

Sends auth token to /token endpoint of the provider in exchange for credentials.

```java
api.token("your-auth-token").call(new AuthenticationCallback() {
    @Override
    public void onSuccess(Credentials credentials) {
        runOnUiThread(() -> {
            // do ui related work
        });
    }

    @Override
    public void onFailure(AuthenticationException e) {
        Log.e("TAG", "Could exchange auth token", e);
    }
});
```


### Renew Credential with a Refresh Token

Renew your credentials using a refresh token. Note that 'offline_access' scope is required to get a refresh refresh token at login, which is added by default.

```java

api.renewAuth().call(new AuthenticationCallback() {
    @Override
    public void onSuccess(Credentials credentials) {
        runOnUiThread(() -> {
            // do ui related work
        });
    }

    @Override
    public void onFailure(AuthenticationException e) {
        Log.e("TAG", "Could not renew auth", e);
    }
});
```

### Revoke a Token

Revoke a token using the revokeToken method. No arg overload only revokes the stored refresh token:

```java
api.revokeToken().call(new VoidCallback() {
    @Override
    public void onSuccess(Void aVoid) {
        runOnUiThread(() -> {
            // do ui related work
        });
    }

    @Override
    public void onFailure(AuthenticationException e) {
        Log.e("TAG", "Could not revoke token", e);
    }
});
```

## Advance Usage


### Storage

By default library uses SharedPreferences in Private mode(can only be accessed by this application) for persistence. You are free to implement the Storage interface with your preferred storage backend. 


Configure your Storage:

```java

OIDC oidc = new OIDCBuilder(
		context,
		"your-client-id",
		"your-oidc-domain")
        .setStorage(new Storage() {
                        @Override
                        public void write(@NonNull String s, @Nullable String s1) {
                            
                        }

                        @Override
                        public void delete(@NonNull String s) {

                        }

                        @Override
                        public String read(@NonNull String s) {
                            return null;
                        }
                    })
    .build();
```

### Encryption

By default library does not use any encryption when storing credentials. You are free to implement the Encryptor interface with your preferred encryption method. We provide AESEncryptor class for 256 bit AES GCM implementation which can be used from Android Api 23 and up.

Configure your Encryptor:

```java

OIDC oidc = new OIDCBuilder(
		context,
		"your-client-id",
		"your-oidc-domain")
        .setEncryptor(new Encryptor() {
                        @Override
                        public String encrypt(String s) {
                            return null;
                        }

                        @Override
                        public String decrypt(String s) {
                            return null;
                        }
                    })
    .build();
```

## Example App

We built a very simple app demonstrating login/logout and fetching user info. Check it out [here](https://github.com/PlusAuth/plusauth-oidc-android-example).

## Acknowledgements

Design of this library was inspired by awesome OSS libraries such as [AppAuth](https://github.com/openid/AppAuth-Android).

If you have used OIDC with any Android library, you might have felt overwhelmed. We did too. That is why we built PlusAuth OIDC Library For Android. We hope to lower the entry barrier for OIDC complexity so that all developers could enjoy benefits that it brings.

# License

This project is licensed under the MIT license. See the [LICENSE](LICENSE) file for more info.

