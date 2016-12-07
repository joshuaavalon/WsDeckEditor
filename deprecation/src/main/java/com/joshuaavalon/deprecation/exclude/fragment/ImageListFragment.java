package com.joshuaavalon.deprecation.exclude.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;

import com.joshuaavalon.wsdeckeditor.exclude.BitmapUtils;
import com.joshuaavalon.wsdeckeditor.exclude.CardImageHolder;
import com.joshuaavalon.wsdeckeditor.sdk.card.Card;
import com.joshuaavalon.wsdeckeditor.task.LoadCircularCardImageTask;

public abstract class ImageListFragment extends BaseFragment {
    private LruCache<String, Bitmap> bitmapCache;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bitmapCache = BitmapUtils.createBitmapCache();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bitmapCache.evictAll();
    }

    protected void loadImage(@Nullable final Card card, @NonNull final CardImageHolder holder) {
        if (card != null) {
            final Bitmap squareBitmap = bitmapCache.get(card.getImage());
            if (squareBitmap != null) {
                holder.getImageView().setImageDrawable(BitmapUtils.toRoundDrawable(getResources(), squareBitmap));
                return;
            }
        }
        holder.getImageView().setImageDrawable(null);
        new LoadCircularCardImageTask(getContext(), bitmapCache, holder, card).execute();
    }
}
