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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AbstractDeck)) return false;
        if (obj == this) return true;
        final AbstractDeck that = (AbstractDeck) obj;
        return name.equals(that.name) && id == that.id;
    }
}
