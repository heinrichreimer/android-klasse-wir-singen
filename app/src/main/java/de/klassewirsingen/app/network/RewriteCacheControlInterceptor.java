package de.klassewirsingen.app.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import okhttp3.Interceptor;
import okhttp3.Response;

import java.io.IOException;
import java.lang.ref.WeakReference;

class RewriteCacheControlInterceptor implements Interceptor {

    private WeakReference<Context> context;

    RewriteCacheControlInterceptor(Context context) {
        this.context = new WeakReference<>(context);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());

        boolean isConnected;
        if (context.isEnqueued()) {
            isConnected = false;
        } else {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.get()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
        }

        if (isConnected) {
            int maxAge = 60; //read from cache for 1 minute
            return originalResponse.newBuilder()
                    .header("Cache-Control", "public, max-age=" + maxAge)
                    .build();
        } else {
            int maxStale = 60 * 60 * 24 * 28; //tolerate 4-weeks stale
            return originalResponse.newBuilder()
                    .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                    .build();
        }
    }
}
