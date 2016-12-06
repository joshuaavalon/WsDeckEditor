package com.joshuaavalon.wsdeckeditor.sdk.deck;

import android.support.annotation.NonNull;

public class DeckRecord {
    @NonNull
    private final String serial;
    private final int count;

    public DeckRecord(@NonNull String serial, int count) {
        this.serial = serial;
        this.count = count;
    }

    @NonNull
    public String getSerial() {
        return serial;
    }

    public int getCount() {
        return count;
    }
}
