package de.klassewirsingen.app.events;

import android.net.Uri;
import android.support.annotation.Nullable;

public class UrlLoadSuccessEvent {
    private String html;
    private Uri url;
    @Nullable
    private Uri historyUrl;

    public UrlLoadSuccessEvent(String html, Uri url, @Nullable Uri historyUrl) {
        this.html = html;
        this.url = url;
        this.historyUrl = historyUrl;
    }

    public UrlLoadSuccessEvent(String html, Uri url) {
        this.html = html;
        this.url = url;
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
}
