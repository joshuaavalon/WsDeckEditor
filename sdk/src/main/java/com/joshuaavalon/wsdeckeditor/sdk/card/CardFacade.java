package com.joshuaavalon.wsdeckeditor.sdk.card;

import android.content.Context;
import android.support.annotation.NonNull;

public final class CardFacade {
    @NonNull
    public static final String DATABASE_NAME = "wsdb.db";

    @NonNull
    public static ICardRepository Repository(@NonNull final Context context) {
        return CacheCardRepository
                .builder(new CardRepository(context, new CardDatabase(context)))
                .build();
    }
}
