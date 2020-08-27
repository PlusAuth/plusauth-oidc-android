package com.plusauth.android.api.request;


import com.plusauth.android.BaseException;
import com.plusauth.android.util.PACallback;

public interface Request<T, U extends BaseException> {

    /**
     * Sends the http request asynchronously, does not block the current thread.
     *
     * @param callback callback for sending results of this call
     */
    void call(PACallback<T, U> callback);

    /**
     * Sends the http request synchronously, blocking the current thread.
     *
     * @return the response on success
     * @throws BaseException on failure
     */
    T callSync() throws BaseException;
}
