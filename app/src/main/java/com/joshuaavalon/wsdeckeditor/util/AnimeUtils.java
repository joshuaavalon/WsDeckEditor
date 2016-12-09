package com.joshuaavalon.wsdeckeditor.util;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;

public final class AnimeUtils {
    public static Bundle createRevealOption(@NonNull final View view) {
        return ActivityOptionsCompat.makeClipRevealAnimation(view, view.getWidth() / 2, view.getHeight() / 2, 50, 50)
                .toBundle();
    }
}
