package com.joshuaavalon.wsdeckeditor.sdk.deck;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

public interface IDeckRepository {
    void save(@NonNull Deck deck);

    void remove(@NonNull Deck deck);

    void add(long id, @NonNull String serial, boolean requireNone);

    void update(long id, @NonNull String serial, int count);

    @NonNull
    List<DeckMeta> abstractDecks();

    @Nullable
    DeckMeta abstractDeck(long id);

    @Nullable
    Deck deck(long id);

    int cardCount(long id);
}
