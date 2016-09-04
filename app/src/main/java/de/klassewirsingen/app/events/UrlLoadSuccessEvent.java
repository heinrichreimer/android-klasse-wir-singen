package de.klassewirsingen.app.events;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import java.lang.ref.WeakReference;

public class UrlLoadSuccessEvent {
    private String html;
    private Uri url;
    @Nullable
    private Uri historyUrl;
    private WeakReference<Fragment> fragment;

    public UrlLoadSuccessEvent(Fragment fragment, String html, Uri url, @Nullable Uri historyUrl) {
        this.html = html;
        this.url = url;
        this.historyUrl = historyUrl;
        this.fragment = new WeakReference<>(fragment);
    }

    public UrlLoadSuccessEvent(Fragment fragment, String html, Uri url) {
        this.html = html;
        this.url = url;
        this.fragment = new WeakReference<>(fragment);
    }

    public String getHtml() {
        return html;
    }

    public Uri getUrl() {
        return url;
    }

    @Nullable
    public Uri getHistoryUrl() {
        return historyUrl;
    }

    public Fragment getFragment() {
        if (fragment.isEnqueued()) {
            return null;
        }
        return fragment.get();
    }
}
