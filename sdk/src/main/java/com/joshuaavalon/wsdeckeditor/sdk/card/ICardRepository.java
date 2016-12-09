package com.joshuaavalon.wsdeckeditor.sdk.card;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.Response;

import java.io.InputStream;
import java.util.List;

public interface ICardRepository {
    int version();

    @Nullable
    Card random();

    @Nullable
    Card find(@NonNull String serial);

    @NonNull
    List<Card> findAll(@NonNull List<String> serials);

    @NonNull
    Bitmap imageOf(@Nullable Card card);

    @NonNull
    Bitmap thumbnailOf(@NonNull Bitmap bitmap);

    void updateDatabase(@NonNull InputStream in);

    @NonNull
    List<Card> findAll(@NonNull Filter filter, int limit, int offset);

    @NonNull
    List<String> imageUrls();

    @NonNull
    List<String> expansions();

    void networkVersion(@NonNull Response.Listener<Integer> listener,
                        @Nullable Response.ErrorListener errorListener);

    void needUpdated(@NonNull Response.Listener<Boolean> listener,
                     @Nullable Response.ErrorListener errorListener);
}
