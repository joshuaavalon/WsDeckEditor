package com.joshuaavalon.wsdeckeditor.task;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.joshuaavalon.wsdeckeditor.sdk.card.Card;
import com.joshuaavalon.wsdeckeditor.sdk.card.ICardRepository;
import com.joshuaavalon.wsdeckeditor.util.BitmapUtils;
import com.joshuaavalon.wsdeckeditor.view.CardImageHolder;

public class CircularCardImageLoadTask extends AsyncTask<Resources, Void, Drawable> {
    @NonNull
    private final CardImageHolder viewHolder;
    @NonNull
    private final ICardRepository cardRepository;
    @Nullable
    private final Card card;

    public CircularCardImageLoadTask(@NonNull final ICardRepository cardRepository,
                                     @NonNull final CardImageHolder viewHolder, @Nullable final Card card) {
        this.cardRepository = cardRepository;
        this.viewHolder = viewHolder;
        this.card = card;
    }

    @Override
    @NonNull
    protected Drawable doInBackground(Resources... params) {
        return BitmapUtils.toRoundDrawable(params[0],
                cardRepository.thumbnailOf(cardRepository.imageOf(card)));
    }

    @Override
    protected void onPostExecute(@NonNull final Drawable drawable) {
        if (card == null || card.getImage().equals(viewHolder.getImageName()))
            viewHolder.setImage(drawable);
    }
}