package com.joshuaavalon.wsdeckeditor.sdk.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class AbstractDeck {
    @NonNull
    private final String name;
    private final long id;
    @Nullable
    private final String cover;

    public AbstractDeck(final long id, @NonNull final String name, @Nullable final String cover) {
        this.id = id;
        this.name = name;
        this.cover = cover;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }

    @Nullable
    public String getCover() {
        return cover;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AbstractDeck)) return false;
        if (obj == this) return true;
        final AbstractDeck that = (AbstractDeck) obj;
        return name.equals(that.name) && id == that.id;
    }
}
