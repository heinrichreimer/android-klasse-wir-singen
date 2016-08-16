package de.klassewirsingen.app.webview;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

public class ObservableWebView extends WebView {
private OnScrollChangeListener listener;

public ObservableWebView(Context context) {
    super(context);
}

public ObservableWebView(Context context, AttributeSet attrs) {
    super(context, attrs);
}

public ObservableWebView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
}

@Override
protected void onScrollChanged(int l, int t, int oldL, int oldT) {
    super.onScrollChanged(l, t, oldL, oldT);
    if (listener != null) {
        listener.onScrollChange(this, l, t, oldL, oldT);
    }
}

public void setOnScrollChangeListener(OnScrollChangeListener listener) {
    this.listener = listener;
}

public OnScrollChangeListener getOnScrollChangeListener() {
    return listener;
}

public interface OnScrollChangeListener {
    /**
     * Called when the scroll position of a view changes.
     *
     * @param v          The view whose scroll position has changed.
     * @param scrollX    Current horizontal scroll origin.
     * @param scrollY    Current vertical scroll origin.
     * @param oldScrollX Previous horizontal scroll origin.
     * @param oldScrollY Previous vertical scroll origin.
     */
    void onScrollChange(WebView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY);
}
}