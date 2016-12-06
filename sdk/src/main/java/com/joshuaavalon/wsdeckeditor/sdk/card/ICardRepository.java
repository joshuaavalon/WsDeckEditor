package com.joshuaavalon.wsdeckeditor.sdk.card;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.InputStream;
import java.util.List;

public interface ICardRepository {
    int getVersion();

    @Nullable
    Card find(@NonNull String serial);

    @NonNull
    List<Card> findAll(@NonNull List<String> serials);

    @NonNull
    Bitmap imageOf(@Nullable Card card);

    void updateDatabase(@NonNull InputStream in);

    @NonNull
    List<Card> findAll(@NonNull Filter filter, int limit, int offset);

    @NonNull
    List<String> imageUrls();

    @NonNull
    List<String> expansions();
}
