package com.joshuaavalon.wsdeckeditor.sdk.util;


import android.support.annotation.NonNull;

public class AbstractDeck {
    @NonNull
    private final String name;
    private final long id;

    public AbstractDeck(final long id, @NonNull final String name) {
        this.id = id;
        this.name = name;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }
}
