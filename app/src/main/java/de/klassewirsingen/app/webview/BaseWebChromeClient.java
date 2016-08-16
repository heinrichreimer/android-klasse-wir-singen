package de.klassewirsingen.app.webview;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.webkit.ConsoleMessage;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.orhanobut.logger.Logger;

import de.klassewirsingen.app.R;

public class BaseWebChromeClient extends WebChromeClient {

    @Override
    public void onReceivedTitle(WebView view, String title) {
        Activity activity = (Activity) view.getContext();
        activity.setTitle(title);
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
        new MaterialDialog.Builder(view.getContext())
                .content(message)
                .title(R.string.title_dialog_alert)
                .positiveText(R.string.positive_dialog_alert)
                .negativeText(R.string.negative_dialog_alert)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        result.confirm();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        result.cancel();
                    }
                })
                .show();
        return true;
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
        new MaterialDialog.Builder(view.getContext())
                .content(message)
                .title(R.string.title_dialog_confirm)
                .positiveText(R.string.positive_dialog_confirm)
                .negativeText(R.string.negative_dialog_confirm)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        result.confirm();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        result.cancel();
                    }
                })
                .show();
        return true;
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
        new MaterialDialog.Builder(view.getContext())
                .content(message)
                .title(R.string.title_dialog_prompt)
                .positiveText(R.string.positive_dialog_prompt)
                .negativeText(R.string.negative_dialog_prompt)
                .input(null, defaultValue, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if (TextUtils.isEmpty(input)) {
                            result.confirm();
                            return;
                        }
                        result.confirm(input.toString());
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        EditText input = dialog.getInputEditText();
                        if (input == null) {
                            result.confirm();
                            return;
                        }
                        CharSequence text = input.getText();
                        if (TextUtils.isEmpty(text)) {
                            result.confirm();
                            return;
                        }
                        result.confirm(text.toString());
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        result.cancel();
                    }
                })
                .show();
        return true;
    }

    @Override
    public boolean onJsBeforeUnload(WebView view, String url, String message, final JsResult result) {
        new MaterialDialog.Builder(view.getContext())
                .content(message)
                .title(R.string.title_dialog_confirm)
                .positiveText(R.string.positive_dialog_confirm)
                .negativeText(R.string.negative_dialog_confirm)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        result.confirm();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        result.cancel();
                    }
                })
                .show();
        return true;
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage message) {
        switch (message.messageLevel()) {
            case DEBUG:
                Logger.d("WebView console in %s (line %s): %s", message.sourceId(),
                        message.lineNumber(), message.message());
                break;
            case ERROR:
                Logger.e("WebView console in %s (line %s): %s", message.sourceId(),
                        message.lineNumber(), message.message());
                break;
            case LOG:
                Logger.i("WebView console in %s (line %s): %s", message.sourceId(),
                        message.lineNumber(), message.message());
                break;
            case TIP:
                Logger.v("WebView console in %s (line %s): %s", message.sourceId(),
                        message.lineNumber(), message.message());
                break;
            case WARNING:
                Logger.w("WebView console in %s (line %s): %s", message.sourceId(),
                        message.lineNumber(), message.message());
                break;
            default:
                Logger.d("WebView console in %s (line %s): %s", message.sourceId(),
                        message.lineNumber(), message.message());
                break;
        }
        return true;
    }

}
