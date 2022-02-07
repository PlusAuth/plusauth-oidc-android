package com.plusauth.android.auth;

/**
 * Abstract class for Login/Logout/X managers.
 */
public abstract class ResumableManager {
    public abstract void resume(AuthorizeResult result);
}
