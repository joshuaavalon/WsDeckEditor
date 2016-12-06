package com.joshuaavalon.wsdeckeditor.exclude;

import android.support.annotation.NonNull;
import android.widget.ImageView;

public interface CardImageHolder {
    @NonNull
    ImageView getImageView();

    @NonNull
    String getImageName();
}
