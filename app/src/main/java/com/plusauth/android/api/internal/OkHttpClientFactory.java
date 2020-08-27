package com.plusauth.android.api.internal;

import android.os.Build;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.ConnectionSpec;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Factory class for creating OkHttpClient instances with customization options and safe defaults.
 */
public class OkHttpClientFactory {
    /**
     * Creates a new OkHttpClient instance with Tls1.2 support.
     *
     * @param loggingEnabled enables http request/response logging
     * @return configured OkHttpClient instance
     */
    public OkHttpClient createClient(boolean loggingEnabled) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        enforceTls12(builder);

        if (loggingEnabled) {
            enableLogging(builder);
        }

        builder.protocols(Arrays.asList(Protocol.HTTP_1_1, Protocol.HTTP_2));
        return builder.build();
    }

    private void enableLogging(OkHttpClient.Builder client) {
        Interceptor interceptor = new HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BODY);
        client.interceptors().add(interceptor);
    }

    /**
     * Enable TLS 1.2 on the OkHttpClient on API 16-21, which is not enabled by default.
     */
    private void enforceTls12(OkHttpClient.Builder builder) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null );
            X509TrustManager trustManager = (X509TrustManager) trustManagerFactory.getTrustManagers()[0];

            SSLContext sc = SSLContext.getInstance(TlsVersion.TLS_1_2.javaName());
            sc.init(null, null, null);
            builder.sslSocketFactory(new TLS12SocketFactory(sc.getSocketFactory()), trustManager);

            ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2)
                    .build();

            List<ConnectionSpec> specs = new ArrayList<>();
            specs.add(cs);
            specs.add(ConnectionSpec.COMPATIBLE_TLS);
            specs.add(ConnectionSpec.CLEARTEXT);

            builder.connectionSpecs(specs);
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new RuntimeException("Error while setting TLS 1.2", e);
        }
    }
}
