package com.joshuaavalon.wsdeckeditor.sdk.deck;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Objects;

public class DeckMeta {
    @NonNull
    private final String name;
    private final long id;
    @Nullable
    private final String cover;

    public DeckMeta(final long id, @NonNull final String name, @Nullable final String cover) {
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
        if (!(obj instanceof DeckMeta)) return false;
        if (obj == this) return true;
        final DeckMeta that = (DeckMeta) obj;
        return name.equals(that.name) && id == that.id && Objects.equal(cover, that.cover);
    }
}
