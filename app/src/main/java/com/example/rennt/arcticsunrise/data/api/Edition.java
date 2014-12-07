package com.example.rennt.arcticsunrise.data.api;

import android.content.Context;

import com.example.rennt.arcticsunrise.R;

import java.util.Locale;

public enum Edition {
    USA("us", Locale.US, R.string.edition_usa),
    EUROPE("europe", Locale.US, R.string.edition_europe),
    ASIA("asia", Locale.US, R.string.edition_asia),
    KOREA("korea", Locale.KOREAN, R.string.edition_korea),
    JAPAN("japan", Locale.JAPANESE, R.string.edition_korea);

    private final String path;
    private final Locale locale;
    private final int displayStringId;

    private Edition(final String path, final Locale locale, final int displayStringId) {
        this.path = path;
        this.locale = locale;
        this.displayStringId = displayStringId;
    }

    public String getPath(){ return path; }
    public Locale getLocale() { return locale; }

    public String getDisplayName(Context context){
        return context.getString(this.displayStringId);
    }
}
