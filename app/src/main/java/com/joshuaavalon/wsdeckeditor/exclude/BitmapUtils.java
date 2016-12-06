package com.joshuaavalon.wsdeckeditor.exclude;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.util.LruCache;
import android.view.Gravity;

public class BitmapUtils {
    @NonNull
    public static Drawable toRoundDrawable(@NonNull final Resources resources,
                                           @NonNull final Bitmap squareBitmap) {
        final RoundedBitmapDrawable roundedBitmap = RoundedBitmapDrawableFactory.create(resources,
                squareBitmap);
        roundedBitmap.setCircular(true);
        roundedBitmap.setGravity(Gravity.CENTER);
        return roundedBitmap;
    }

    public static <T> LruCache<T, Bitmap> createBitmapCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        return new LruCache<T, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(T key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }
}
