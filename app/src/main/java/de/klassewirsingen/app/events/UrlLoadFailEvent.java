package de.klassewirsingen.app.events;

import android.net.Uri;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public class UrlLoadFailEvent {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Reason.UNKNOWN, Reason.NETWORK, Reason.NO_CONTEXT, Reason.NO_CONNECTION, Reason.TIMEOUT})
    public @interface Reason {
        int UNKNOWN = -1;
        int NETWORK = 0;
        int NO_CONTEXT = 1;
        int NO_CONNECTION = 2;
        int TIMEOUT = 3;
    }

    @Reason
    private int reason = Reason.UNKNOWN;
    private Uri url;

    public UrlLoadFailEvent(Uri url) {
        this.url = url;
    }

    public UrlLoadFailEvent(@Reason int reason, Uri url) {
        this.reason = reason;
        this.url = url;
    }

    @Reason
    public int getReason() {
        return reason;
    }

    public Uri getUrl() {
        return url;
    }
}
