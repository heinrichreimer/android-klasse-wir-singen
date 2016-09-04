package de.klassewirsingen.app.app;


import android.annotation.SuppressLint;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.orhanobut.logger.Logger;
import de.klassewirsingen.app.BuildConfig;
import de.klassewirsingen.app.R;
import de.klassewirsingen.app.databinding.FragmentWebBinding;
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

import static de.klassewirsingen.app.app.MainActivity.HOME_URI;

public class WebFragment extends Fragment {
    private static final String ARG_URL = "de.klassewirsingen.app.ARG_URL";

    private static final String KEY_HTML = "de.klassewirsingen.app.KEY_HTML";
    private static final String KEY_URL = "de.klassewirsingen.app.KEY_URL";

    private String currentHtml;
    private Uri currentUrl;

    private FragmentWebBinding binding;

    private LoadUrlTask loadUrlTask;

    public WebFragment() {
    }

    public static WebFragment newInstance(Uri url) {
        WebFragment fragment = new WebFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

        if (savedInstanceState != null && !TextUtils.isEmpty(savedInstanceState.getString(KEY_HTML, null)) &&
                !TextUtils.isEmpty(savedInstanceState.getString(KEY_URL, null))) {
            currentHtml = savedInstanceState.getString(KEY_HTML);
            currentUrl = Uri.parse(savedInstanceState.getString(KEY_URL));
        } else {
            if (getArguments() != null) {
                currentUrl = getArguments().getParcelable(ARG_URL);
            } else {
                currentUrl = HOME_URI;
            }

        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_web, container, false);

        binding = DataBindingUtil.bind(root);

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
        binding.webView.getSettings().setAppCachePath(getContext().getCacheDir().getPath());
        binding.webView.setOnScrollChangeListener(new SwipeRefreshScrollListener());
        binding.webView.setWebChromeClient(new BaseWebChromeClient());
        binding.webView.setWebViewClient(new BaseWebViewClient());
        binding.webView.setBackgroundColor(Color.TRANSPARENT);

        if (!TextUtils.isEmpty(currentHtml)) {
            EventBus.getDefault().post(new UrlLoadSuccessEvent(this, currentHtml, currentUrl));
        } else {
            loadUrl(currentUrl);
        }

        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_URL, currentUrl == null ? null : currentUrl.toString());
        //outState.putString(KEY_HTML, currentHtml);
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

        if (Build.VERSION.SDK_INT >= 11) {
            binding.webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        }

        loadUrlTask = new LoadUrlTask(this, url, currentUrl);
        loadUrlTask.execute();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUrlLoadFail(UrlLoadFailEvent event) {
        if (event.getFragment() != this) {
            return;
        }

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
        if (event.getFragment() != this) {
            return;
        }

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
            } else {
                binding.swipeRefresh.setEnabled(true);
            }
        }
    }
}
