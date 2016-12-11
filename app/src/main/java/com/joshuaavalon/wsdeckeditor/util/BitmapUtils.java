package com.joshuaavalon.wsdeckeditor.util;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.Gravity;

import com.joshuaavalon.wsdeckeditor.BuildConfig;
import com.joshuaavalon.wsdeckeditor.R;

import java.io.File;
import java.io.FileOutputStream;

import timber.log.Timber;

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

    public static Bitmap rotate(@NonNull final Bitmap bitmap, final float angle) {
        final Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static void share(@NonNull final Context context, @NonNull final Bitmap bitmap) {
        try {
            final File cachePath = new File(context.getCacheDir(), "deck");
            if (!cachePath.mkdirs())
                Timber.d("Make directory failed.");
            final File file = new File(cachePath, "share.png");
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
            final Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
            final Intent intent = new Intent(Intent.ACTION_SEND);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType(context.getContentResolver().getType(uri));
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_your_deck)));
        } catch (Exception e) {
            Timber.e(e, "Share Bitmap");
        }
    }
}
