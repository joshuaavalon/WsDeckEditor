package com.joshuaavalon.wsdeckeditor.task;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.joshuaavalon.wsdeckeditor.sdk.card.Card;
import com.joshuaavalon.wsdeckeditor.sdk.card.ICardRepository;
import com.joshuaavalon.wsdeckeditor.view.CardImageHolder;

public class CardImageLoadTask extends AsyncTask<Void, Void, Bitmap> {
    @NonNull
    private final CardImageHolder viewHolder;
    @NonNull
    private final ICardRepository cardRepository;
    @NonNull
    private final Card card;

    public CardImageLoadTask(@NonNull final ICardRepository cardRepository,
                             @NonNull final CardImageHolder viewHolder,
                             @NonNull final Card card) {
        this.cardRepository = cardRepository;
        this.viewHolder = viewHolder;
        this.card = card;
    }

    @Override
    @NonNull
    protected Bitmap doInBackground(Void... params) {
        return cardRepository.imageOf(card);
    }

    @Override
    protected void onPostExecute(@NonNull final Bitmap bitmap) {
        if (card.getImage().equals(viewHolder.getImageName()))
            viewHolder.setImage(bitmap);
    }
}
