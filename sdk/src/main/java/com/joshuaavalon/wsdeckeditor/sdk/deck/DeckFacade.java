package com.joshuaavalon.wsdeckeditor.sdk.deck;

import android.content.Context;
import android.support.annotation.NonNull;

import com.joshuaavalon.wsdeckeditor.sdk.card.CardFacade;
import com.joshuaavalon.wsdeckeditor.sdk.card.ICardRepository;

public class DeckFacade {
    @NonNull
    public static final String DATABASE_NAME = "deck.db";

    @NonNull
    public static IDeckRepository Repository(@NonNull final Context context,
                                             @NonNull final ICardRepository repository) {
        return new DeckRepository(new DeckDatabase(context), repository);
    }
}
