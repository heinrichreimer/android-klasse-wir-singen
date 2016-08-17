package de.klassewirsingen.app.webview;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.orhanobut.logger.Logger;
import de.klassewirsingen.app.R;
import de.klassewirsingen.app.app.MainActivity;

import java.util.List;

public class BaseWebViewClient extends WebViewClient{

    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return shouldOverrideUrlLoadingImpl(view, Uri.parse(url));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return shouldOverrideUrlLoadingImpl(view, request.getUrl());
    }

    private boolean shouldOverrideUrlLoadingImpl(WebView view, final Uri url) {
        if (!(view.getContext() instanceof MainActivity)) {
            Logger.d("Overriding url loading for url \"%s\": default WebView behavior", url);
            return false;
        }

        Activity activity = (Activity) view.getContext();

        if ((TextUtils.equals(url.getScheme(), "http") ||
                TextUtils.equals(url.getScheme(), "https")) &&
                (TextUtils.equals(url.getHost(), "klasse-wir-singen.de") ||
                        TextUtils.equals(url.getHost(), "www.klasse-wir-singen.de"))) {

            List<String> path = url.getPathSegments();
            String mime = MimeTypeMap.getFileExtensionFromUrl(url.toString()).toLowerCase();

            if (TextUtils.equals(mime, "audio/mpeg3") || TextUtils.equals(mime, "audio/x-mpeg-3") ||
                    TextUtils.equals(mime, "video/mpeg") || TextUtils.equals(mime, "video/x-mpeg") ||
                    TextUtils.equals(mime, "audio/mpeg") || TextUtils.equals(mime, "audio/mp3") ||
                    url.toString().toLowerCase().endsWith(".mp3")) {
                //The app can't play MP3s
                launchExternal(activity, url);
                return true;
            }
            if (TextUtils.equals(mime, "image/gif") || TextUtils.equals(mime, "image/jpeg") ||
                    TextUtils.equals(mime, "image/png") ||
                    url.toString().toLowerCase().endsWith(".jpeg") ||
                    url.toString().toLowerCase().endsWith(".jpg") ||
                    url.toString().toLowerCase().endsWith(".jpe") ||
                    url.toString().toLowerCase().endsWith(".png") ||
                    url.toString().toLowerCase().endsWith(".gif")) {
                //The app can't play MP3s
                launchExternal(activity, url);
                return true;
            }
            if (TextUtils.equals(mime, "application/pdf") || TextUtils.equals(mime, "application/x-pdf") ||
                    url.toString().toLowerCase().endsWith(".pdf")) {
                //The app can't display PDFs
                launchExternal(activity, url);
                return true;
            }
            if (path != null && !path.isEmpty()) {
                if (path.size() == 2 && TextUtils.equals(path.get(0), "anmeldung-tickets") &&
                        TextUtils.equals(path.get(1), "anmeldung")) {
                    //The booking form should not be opened in the app
                    launchExternal(activity, url);
                    return true;
                }
            }

            launchInternal(activity, url);
            return true;
        }
        launchExternal(activity, url);
        return true;
    }

    private void launchExternal(Activity activity, Uri url) {
        new CustomTabsIntent.Builder()
                .setToolbarColor(ContextCompat.getColor(activity, R.color.color_primary))
                .setSecondaryToolbarColor(ContextCompat.getColor(activity, R.color.color_primary_dark))
                .setStartAnimations(activity, R.anim.fade_in, R.anim.fade_out)
                .setExitAnimations(activity, R.anim.fade_in, R.anim.fade_out)
                .build()
                .launchUrl(activity, url);

        Logger.d("Overriding url loading for url \"%s\": default web intent", url);
    }

    private void launchInternal(Activity activity, Uri url) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(url);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        Logger.d("Overriding url loading for url \"%s\": load url internal", url);
    }
}
