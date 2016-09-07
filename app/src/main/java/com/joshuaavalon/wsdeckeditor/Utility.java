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

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;

public class Utility {
    public static String getImageNameFromUrl(@NonNull final String url) {
        return Uri.parse(url).getLastPathSegment();
    }

    public static boolean requestPermission(Activity activity, int requestCode, String... permissions) {
        boolean granted = true;
        ArrayList<String> permissionsNeeded = new ArrayList<>();

        for (String s : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(activity, s);
            boolean hasPermission = (permissionCheck == PackageManager.PERMISSION_GRANTED);
            granted &= hasPermission;
            if (!hasPermission) {
                permissionsNeeded.add(s);
            }
        }

        if (granted) {
            return true;
        } else {
            ActivityCompat.requestPermissions(activity,
                    permissionsNeeded.toArray(new String[permissionsNeeded.size()]),
                    requestCode);
            return false;
        }
    }

    public static boolean permissionGranted(
            int requestCode, int permissionCode, int[] grantResults) {
        return requestCode == permissionCode &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }

    @NonNull
    public static String toString(Collection<String> stringCollection, String separator) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (String string : stringCollection) {
            if (!stringBuilder.toString().equals(""))
                stringBuilder.append(separator);
            stringBuilder.append(string);
        }
        return stringBuilder.toString();
    }

    @SuppressLint("SetWorldReadable")
    public static Uri savePublicBitmap(@NonNull final Bitmap bitmap, @NonNull final String fileName) {
        try {
            final File file = new File(WsApplication.getContext().getCacheDir(), fileName + ".png");
            final FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
            if (!file.setReadable(true, false))
                Log.e("Bitmap", "Share failed");
            return Uri.fromFile(file);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static void sharePublicBitmap(@NonNull final Activity activity, @NonNull final Uri uri) {
        final Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType("image/png");
        activity.startActivity(shareIntent);
    }
}
