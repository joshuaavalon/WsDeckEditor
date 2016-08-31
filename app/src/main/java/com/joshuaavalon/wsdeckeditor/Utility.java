package com.joshuaavalon.wsdeckeditor;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

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


    public static boolean requestPermission(Fragment fragment, int requestCode, String... permissions) {
        boolean granted = true;
        ArrayList<String> permissionsNeeded = new ArrayList<>();

        for (String s : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(fragment.getContext(), s);
            boolean hasPermission = (permissionCheck == PackageManager.PERMISSION_GRANTED);
            granted &= hasPermission;
            if (!hasPermission) {
                permissionsNeeded.add(s);
            }
        }

        if (granted) {
            return true;
        } else {
            fragment.requestPermissions(permissionsNeeded.toArray(new String[permissionsNeeded.size()]),
                    requestCode);
            return false;
        }
    }

    public static String toString(Collection<String> stringCollection, String separator) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String string : stringCollection) {
            if (!stringBuilder.toString().equals(""))
                stringBuilder.append(separator);
            stringBuilder.append(string);
        }
        return stringBuilder.toString();
    }
}
