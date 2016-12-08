package com.joshuaavalon.wsdeckeditor.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
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
}