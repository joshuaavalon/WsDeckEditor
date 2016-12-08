package com.joshuaavalon.wsdeckeditor.view;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

public interface CardImageHolder {
    void setImage(@NonNull Bitmap bitmap);

    void setImage(@NonNull Drawable drawable);

    @NonNull
    String getImageName();
}
