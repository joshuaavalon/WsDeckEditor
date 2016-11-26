package com.joshuaavalon.wsdeckeditor.sdk.task;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.joshuaavalon.wsdeckeditor.sdk.Card;
import com.joshuaavalon.wsdeckeditor.sdk.data.CardRepository;

import java.util.List;

public abstract class CardLoadTask extends ResultTask<Context, List<Card>> {
    protected CardLoadTask(@Nullable final CallBack<List<Card>> callBack) {
        super(callBack);
    }

    @NonNull
    protected static Card buildCard(@NonNull final Cursor cursor) {
        return CardRepository.buildCard(cursor);
    }
}
