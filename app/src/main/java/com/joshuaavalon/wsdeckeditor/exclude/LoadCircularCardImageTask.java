package com.joshuaavalon.wsdeckeditor.exclude;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;

import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.sdk.card.Card;
import com.joshuaavalon.wsdeckeditor.sdk.card.CardFacade;
import com.joshuaavalon.wsdeckeditor.sdk.card.ICardRepository;
import com.joshuaavalon.wsdeckeditor.util.ThumbnailUtils;

public class LoadCircularCardImageTask extends AsyncTask<Void, Void, Drawable> {
    @NonNull
    private final CardImageHolder viewHolder;
    @Nullable
    private final Card card;
    @NonNull
    private final Context context;
    @Nullable
    private final LruCache<String, Bitmap> bitmapCache;

    public LoadCircularCardImageTask(@NonNull final Context context, @Nullable final LruCache<String, Bitmap> bitmapCache,
                                     @NonNull final CardImageHolder viewHolder, @Nullable final Card card) {
        this.context = context.getApplicationContext();
        this.bitmapCache = bitmapCache;
        this.viewHolder = viewHolder;
        this.card = card;
    }

    @Override
    @NonNull
    protected Drawable doInBackground(Void... params) {
        ICardRepository repository = CardFacade.Repository(context);
        final Bitmap bitmap = repository.imageOf(card);
        final int dimension = context.getResources().getDimensionPixelSize(R.dimen.detail_item_icon_size);
        final Bitmap squareBitmap = ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension);
        if (bitmapCache != null && card != null)
            bitmapCache.put(card.getImage(), squareBitmap);
        return BitmapUtils.toRoundDrawable(context.getResources(), squareBitmap);
    }

    @Override
    protected void onPostExecute(@NonNull final Drawable drawable) {
        if (card == null || card.getImage().equals(viewHolder.getImageName()))
            viewHolder.getImageView().setImageDrawable(drawable);
    }
}