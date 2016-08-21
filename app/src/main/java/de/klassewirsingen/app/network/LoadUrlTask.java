package de.klassewirsingen.app.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import de.klassewirsingen.app.events.UrlLoadFailEvent;
import de.klassewirsingen.app.events.UrlLoadSuccessEvent;
import de.klassewirsingen.app.webview.InternalResourcesInflater;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;

public class LoadUrlTask extends AsyncTask<Void, Void, String> {
    private OkHttpClient client;

    private WeakReference<Context> context;

    @NotNull
    @NonNull
    private Uri url;
    @Nullable
    private Uri historyUrl;
    @UrlLoadFailEvent.Reason
    private int reason = UrlLoadFailEvent.Reason.UNKNOWN;

    public LoadUrlTask(Context context, @NotNull @NonNull Uri url, @Nullable Uri historyUrl) {
        this.context = new WeakReference<>(context);
        this.url = url;
        this.historyUrl = historyUrl;

        File httpCacheDirectory = new File(context.getCacheDir(), "responses");
        int cacheSize = 10 * 1024 * 1024; //10mb
        Cache cache = new Cache(httpCacheDirectory, cacheSize);

        client = new OkHttpClient.Builder()
                .cache(cache)
                .addNetworkInterceptor(new RewriteCacheControlInterceptor(context))
                .build();
    }

    @Nullable
    @Override
    protected String doInBackground(Void... args) {
        if (context.isEnqueued()) {
            reason = UrlLoadFailEvent.Reason.NO_CONTEXT;
            cancel(true);
            return null;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.get()
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (!isConnected) {
            reason = UrlLoadFailEvent.Reason.NO_CONNECTION;
            cancel(true);
            return null;
        }

        Request request = new Request.Builder()
                .url(url.toString())
                .build();

        try {
            Response response = client.newCall(request).execute();

            String html = response.body().string();

            if (context.isEnqueued()) {
                reason = UrlLoadFailEvent.Reason.NO_CONTEXT;
                cancel(true);
                return null;
            }

            html = InternalResourcesInflater.inflate(context.get(), html, url);

            return html;
        } catch (IOException e) {
            if (e instanceof SocketTimeoutException) {
                reason = UrlLoadFailEvent.Reason.TIMEOUT;
            }
            reason = UrlLoadFailEvent.Reason.NETWORK;
            cancel(true);
            return null;
        }
    }

    @Override
    protected void onPostExecute(@Nullable String html) {
        if (html == null) {
            onCancelled();
        }
        EventBus.getDefault().post(new UrlLoadSuccessEvent(html, url, historyUrl));
    }

    @Override
    protected void onCancelled() {
        EventBus.getDefault().post(new UrlLoadFailEvent(reason, url));
    }
}