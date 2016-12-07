package com.joshuaavalon.wsdeckeditor.task;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.joshuaavalon.wsdeckeditor.sdk.card.Card;
import com.joshuaavalon.wsdeckeditor.sdk.card.ICardRepository;
import com.joshuaavalon.wsdeckeditor.util.BitmapUtils;
import com.joshuaavalon.wsdeckeditor.view.CardImageHolder;

public class LoadCircularCardImageTask extends AsyncTask<Resources, Void, Drawable> {
    @NonNull
    private final CardImageHolder viewHolder;
    @NonNull
    private final ICardRepository cardRepository;
    @NonNull
    private final Card card;

    public LoadCircularCardImageTask(@NonNull final ICardRepository cardRepository,
                                     @NonNull final CardImageHolder viewHolder, @NonNull final Card card) {
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
        if (card.getImage().equals(viewHolder.getImageName()))
            viewHolder.getImageView().setImageDrawable(drawable);
    }
}