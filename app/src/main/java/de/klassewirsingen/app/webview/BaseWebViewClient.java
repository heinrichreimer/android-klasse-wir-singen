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

import java.util.regex.Pattern;

public class BaseWebViewClient extends WebViewClient{

    private static final Pattern PATH_REGEX = Pattern.compile("(?i)^/(?!anmeldung-tickets/anmeldung)(?!.*\\.(jpe?g|jpe|png|gif|pdf|mp3)$).*$");
    private static final Pattern IMAGE_REGEX = Pattern.compile("(?i)^/(?!anmeldung-tickets/anmeldung).*\\.(jpe?g|jpe|png|gif|pdf|mp3)$");

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

            String path = url.getPath();
            if (PATH_REGEX.matcher(path).matches()) {
                //Matches a path we can open itself
                browseInternally(activity, url);
                return true;
            }

            if (IMAGE_REGEX.matcher(path).matches()) {
                viewExternally(activity, url);
            }
        }
        browseExternally(activity, url);
        return true;
    }

    private void viewExternally(Activity activity, Uri url) {
        String mime = MimeTypeMap.getFileExtensionFromUrl(url.toString()).toLowerCase();

        Intent intent = new Intent(Intent.ACTION_VIEW, url);
        intent.setType(mime);

        try {
            activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            activity.startActivity(intent);
        } catch (Exception e) {
            browseExternally(activity, url);
        }

        Logger.d("Overriding url loading for url \"%s\": default web intent", url);
    }

    private void browseExternally(Activity activity, Uri url) {
        new CustomTabsIntent.Builder()
                .setToolbarColor(ContextCompat.getColor(activity, R.color.color_primary))
                .setSecondaryToolbarColor(ContextCompat.getColor(activity, R.color.color_primary_dark))
                .setStartAnimations(activity, R.anim.fade_in, R.anim.fade_out)
                .setExitAnimations(activity, R.anim.fade_in, R.anim.fade_out)
                .setShowTitle(true)
                .build()
                .launchUrl(activity, url);

        Logger.d("Overriding url loading for url \"%s\": default web intent", url);
    }

    private void browseInternally(Activity activity, Uri url) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(url);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        Logger.d("Overriding url loading for url \"%s\": load url internal", url);
    }
}
