package de.klassewirsingen.app.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.webkit.WebView;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.net.SocketTimeoutException;

import de.klassewirsingen.app.BuildConfig;
import de.klassewirsingen.app.R;
import de.klassewirsingen.app.databinding.ActivityMainBinding;
import de.klassewirsingen.app.webview.BaseWebChromeClient;
import de.klassewirsingen.app.webview.BaseWebViewClient;
import de.klassewirsingen.app.webview.InternalResourcesInflater;
import de.klassewirsingen.app.webview.ObservableWebView;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final Uri HOME_URI = Uri.parse("http://klasse-wir-singen.de");

    private ActivityMainBinding binding;

    private OkHttpClient client = new OkHttpClient();
    private Uri currentUrl = null;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.init("KWS");
        super.onCreate(savedInstanceState);

        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.swipeRefresh.setColorSchemeResources(R.color.color_accent, R.color.color_primary);
        binding.swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadUrl(currentUrl);
            }
        });

        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.setOnScrollChangeListener(new SwipeRefreshScrollListener());
        binding.webView.setWebChromeClient(new BaseWebChromeClient());
        binding.webView.setWebViewClient(new BaseWebViewClient());

        Uri url;
        if(getIntent().getAction().equals(Intent.ACTION_VIEW) && getIntent().getData() != null) {
            url = getIntent().getData();
        }
        else {
            url = HOME_URI;
        }
        loadUrl(url);
    }

    public void loadUrl(Uri url) {
        Logger.d("Loading url: %s", url);
        new LoadUrlTask(url, currentUrl).execute();
    }

    private class LoadUrlTask extends AsyncTask<Void, Void, String> {
        @NonNull private Uri url;
        @Nullable private Uri historyUrl;
        private Exception exception;

        public LoadUrlTask(@NonNull Uri url, @Nullable Uri historyUrl) {
            this.url = url;
            this.historyUrl = historyUrl;
        }

        @Override
        protected void onPreExecute() {
            exception = null;
            binding.swipeRefresh.setRefreshing(true);
        }

        @Nullable
        @Override
        protected String doInBackground(Void... args) {
            Request request = new Request.Builder()
                    .url(url.toString())
                    .build();

            try {
                Response response = client.newCall(request).execute();

                String html = response.body().string();
                html = InternalResourcesInflater.inflate(MainActivity.this, html, url);

                return html;
            } catch (IOException e) {
                exception = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(@Nullable String html) {
            currentUrl = url;

            if (html == null) {
                onCancelled();
            }
            binding.swipeRefresh.setRefreshing(false);
            binding.webView.setVisibility(View.VISIBLE);
            binding.errorView.setVisibility(View.GONE);

            binding.webView.loadDataWithBaseURL(
                    url.toString(),
                    html,
                    "text/html",
                    "utf-8",
                    historyUrl != null ? historyUrl.toString() : null);
        }

        @Override
        protected void onCancelled() {
            binding.swipeRefresh.setRefreshing(false);
            binding.webView.setVisibility(View.GONE);
            binding.errorView.setVisibility(View.VISIBLE);

            if(exception instanceof SocketTimeoutException){
                Logger.e(exception, "Timeout while loading url.");
            }
            else if (exception != null) {
                Logger.e(exception, "Unknown error while loading url.");
            }
        }
    }

    private class SwipeRefreshScrollListener implements ObservableWebView.OnScrollChangeListener {
        @Override
        public void onScrollChange(WebView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
            if (scrollY > 0) {
                binding.swipeRefresh.setEnabled(false);
            }
            else {
                binding.swipeRefresh.setEnabled(true);
            }
        }
    }
}
