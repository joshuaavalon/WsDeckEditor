package com.joshuaavalon.wsdeckeditor.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;

import com.joshuaavalon.wsdeckeditor.BitmapUtils;
import com.joshuaavalon.wsdeckeditor.CardImageHolder;
import com.joshuaavalon.wsdeckeditor.LoadCircularCardImageTask;
import com.joshuaavalon.wsdeckeditor.sdk.Card;

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

    protected void loadImage(@NonNull final Card card, @NonNull final CardImageHolder holder) {
        final Bitmap squareBitmap = bitmapCache.get(card.getImage());
        if (squareBitmap != null) {
            holder.getImageView().setImageDrawable(BitmapUtils.toRoundDrawable(getResources(), squareBitmap));
        } else {
            holder.getImageView().setImageDrawable(null);
            new LoadCircularCardImageTask(getContext(), bitmapCache, holder, card).execute();
        }
    }
}
