package com.joshuaavalon.wsdeckeditor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.common.base.Optional;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class Utility {
    public static String getImageNameFromUrl(@NonNull final String url) {
        return Uri.parse(url).getLastPathSegment();
    }

    public static boolean requestPermission(@NonNull final Activity activity,
                                            final int requestCode,
                                            @NonNull final String... permissions) {
        boolean granted = true;
        final ArrayList<String> permissionsNeeded = new ArrayList<>();

        for (String permission : permissions) {
            final int permissionCheck = ContextCompat.checkSelfPermission(activity, permission);
            final boolean hasPermission = (permissionCheck == PackageManager.PERMISSION_GRANTED);
            granted &= hasPermission;
            if (!hasPermission)
                permissionsNeeded.add(permission);
        }

        if (granted)
            return true;
        else {
            ActivityCompat.requestPermissions(activity,
                    permissionsNeeded.toArray(new String[permissionsNeeded.size()]),
                    requestCode);
            return false;
        }
    }

    public static boolean permissionGranted(final int requestCode,
                                            final int permissionCode,
                                            @NonNull final int[] grantResults) {
        return requestCode == permissionCode &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }

    @NonNull
    @SuppressLint("SetWorldReadable")
    public static Optional<Uri> savePublicBitmap(@NonNull final Bitmap bitmap,
                                                 @NonNull final String fileName) {
        try {
            final File file = new File(WsApplication.getContext().getCacheDir(), fileName + ".png");
            final FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
            if (!file.setReadable(true, false))
                Log.e("Bitmap", "Share failed");
            return Optional.of(Uri.fromFile(file));
        } catch (Exception ignored) {
        }
        return Optional.absent();
    }

    public static void sharePublicBitmap(@NonNull final Activity activity, @NonNull final Uri uri) {
        final Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType("image/png");
        activity.startActivity(shareIntent);
    }
}
