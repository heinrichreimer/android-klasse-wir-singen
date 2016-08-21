package de.klassewirsingen.app.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.orhanobut.logger.Logger;
import de.klassewirsingen.app.BuildConfig;
import de.klassewirsingen.app.R;
import de.klassewirsingen.app.databinding.ActivityMainBinding;
import de.klassewirsingen.app.events.UrlLoadFailEvent;
import de.klassewirsingen.app.events.UrlLoadSuccessEvent;
import de.klassewirsingen.app.network.LoadUrlTask;
import de.klassewirsingen.app.webview.BaseWebChromeClient;
import de.klassewirsingen.app.webview.BaseWebViewClient;
import de.klassewirsingen.app.webview.ObservableWebView;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {

    private static final Uri HOME_URI = Uri.parse("http://klasse-wir-singen.de");

    private static final String KEY_HTML = "de.klassewirsingen.app.KEY_HTML";
    private static final String KEY_URL = "de.klassewirsingen.app.KEY_URL";

    private ActivityMainBinding binding;

    private String currentHtml;
    private Uri currentUrl;

    private LoadUrlTask loadUrlTask;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.init("KWS");
        EventBus.getDefault().register(this);

        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        binding.swipeRefresh.setColorSchemeResources(R.color.color_accent, R.color.color_primary);
        binding.swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadUrl(currentUrl);
            }
        });

        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        binding.webView.getSettings().setAppCacheEnabled(true);
        binding.webView.getSettings().setAppCachePath(getCacheDir().getPath());
        binding.webView.setOnScrollChangeListener(new SwipeRefreshScrollListener());
        binding.webView.setWebChromeClient(new BaseWebChromeClient());
        binding.webView.setWebViewClient(new BaseWebViewClient());
        binding.webView.setBackgroundColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= 11) {
            binding.webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        }

        if (savedInstanceState != null && !TextUtils.isEmpty(savedInstanceState.getString(KEY_HTML, null)) &&
                !TextUtils.isEmpty(savedInstanceState.getString(KEY_URL, null))) {
            EventBus.getDefault().post(new UrlLoadSuccessEvent(savedInstanceState.getString(KEY_HTML),
                    Uri.parse(savedInstanceState.getString(KEY_URL))));
        }
        else {
            Uri url;
            if (getIntent().getAction().equals(Intent.ACTION_VIEW) && getIntent().getData() != null) {
                url = getIntent().getData();
            } else {
                url = HOME_URI;
            }
            loadUrl(url);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_URL, currentUrl == null ? null : currentUrl.toString());
        outState.putString(KEY_HTML, currentHtml);
    }

    @Override
    public void onDestroy() {
        if (loadUrlTask != null && !loadUrlTask.isCancelled()) {
            loadUrlTask.cancel(true);
        }
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void loadUrl(@NotNull @NonNull Uri url) {
        Logger.d("Loading url: %s", url);
        currentUrl = url;

        binding.swipeRefresh.setRefreshing(true);

        loadUrlTask = new LoadUrlTask(this, url, currentUrl);
        loadUrlTask.execute();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUrlLoadFail(UrlLoadFailEvent event) {
        binding.swipeRefresh.setRefreshing(false);
        binding.webView.setVisibility(View.GONE);
        binding.errorLayout.setVisibility(View.VISIBLE);

        int errorTextResId;
        int errorIconResId;
        switch (event.getReason()) {
            case UrlLoadFailEvent.Reason.NETWORK:
                errorTextResId = R.string.label_error_network;
                errorIconResId = R.drawable.ic_no_internet;
                break;
            case UrlLoadFailEvent.Reason.NO_CONTEXT:
                errorTextResId = R.string.label_error_no_context;
                errorIconResId = R.drawable.ic_error;
                break;
            case UrlLoadFailEvent.Reason.NO_CONNECTION:
                errorTextResId = R.string.label_error_no_connection;
                errorIconResId = R.drawable.ic_no_internet;
                break;
            case UrlLoadFailEvent.Reason.TIMEOUT:
                errorTextResId = R.string.label_error_timeout;
                errorIconResId = R.drawable.ic_timeout;
                break;
            case UrlLoadFailEvent.Reason.UNKNOWN:
            default:
                errorTextResId = R.string.label_error_unknown;
                errorIconResId = R.drawable.ic_error;
                break;
        }
        binding.errorText.setText(errorTextResId);
        binding.errorText.setCompoundDrawablesWithIntrinsicBounds(0, errorIconResId, 0, 0);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUrlLoadSuccess(UrlLoadSuccessEvent event) {
        currentHtml = event.getHtml();
        currentUrl = event.getUrl();

        binding.swipeRefresh.setRefreshing(false);
        binding.webView.setVisibility(View.VISIBLE);
        binding.errorLayout.setVisibility(View.GONE);

        binding.webView.loadDataWithBaseURL(
                event.getUrl().toString(),
                event.getHtml(),
                "text/html",
                "utf-8",
                event.getHistoryUrl() != null ? event.getHistoryUrl().toString() : null);
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
