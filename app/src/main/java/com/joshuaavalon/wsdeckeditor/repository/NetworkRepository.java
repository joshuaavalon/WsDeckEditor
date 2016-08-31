package com.joshuaavalon.wsdeckeditor.repository;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.joshuaavalon.wsdeckeditor.Handler;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.Utility;
import com.joshuaavalon.wsdeckeditor.WsApplication;
import com.joshuaavalon.wsdeckeditor.database.WsDatabaseHelper;
import com.joshuaavalon.wsdeckeditor.model.Card;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

public class NetworkRepository {
    @NonNull
    public static final String DB_URL = "http://joshuaavalon.com/wp-content/uploads/wsdb.db";
    @NonNull
    public static final String DB_VERSION_URL = "http://joshuaavalon.com/wp-content/uploads/version.json";
    @Nullable
    private static RequestQueue requestQueue;

    public static void downloadDatabase() {
        final Context context = WsApplication.getContext();
        final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        final String url = DB_URL;
        final DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setVisibleInDownloadsUi(false);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, WsDatabaseHelper.DATABASE_NAME);
        final long enqueue = manager.enqueue(request);
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (!DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) return;
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(enqueue);
                final Cursor c = manager.query(query);
                if (!c.moveToFirst()) return;
                context.unregisterReceiver(this);
                int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                if (c.getInt(columnIndex) != DownloadManager.STATUS_SUCCESSFUL) return;
                final WsDatabaseHelper database = new WsDatabaseHelper(context);
                final File dbFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), WsDatabaseHelper.DATABASE_NAME);
                try {
                    database.copyDatabase(new FileInputStream(dbFile));
                } catch (FileNotFoundException ignored) {
                }
                if (!dbFile.delete())
                    Log.e("NetworkRepository", "Clear db cache");
            }
        }, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    public static void downloadImages(@NonNull final List<String> urls, final boolean forced) {
        final Context context = WsApplication.getContext();
        final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (String url : urls) {
                    final String fileName = Utility.getImageNameFromUrl(url);
                    final File image = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
                    if (image.exists())
                        if (!forced)
                            continue;
                        else if (!image.delete())
                            Log.e("NetworkRepository", "Clear image fail:" + fileName);
                    final DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                    request.setVisibleInDownloadsUi(false);
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
                    request.setDestinationInExternalFilesDir(context,
                            Environment.DIRECTORY_PICTURES, Utility.getImageNameFromUrl(url));
                    manager.enqueue(request);
                }
            }
        });
        thread.start();
    }

    public static Bitmap getCardImage(String imageName, Card.Type type) {
        final Context context = WsApplication.getContext();
        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
                type != Card.Type.Climax ? R.drawable.dc_w00_00 : R.drawable.dc_w00_000, null);
        return getImage(imageName, bitmap);
    }

    public static Bitmap getImage(@NonNull final String imageName,
                                  @NonNull final Bitmap defaultBitmap) {
        final Context context = WsApplication.getContext();
        Bitmap bitmap = defaultBitmap;
        final File image = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                imageName);
        if (image.exists()) {
            BitmapFactory.Options option = new BitmapFactory.Options();
            option.inDensity = DisplayMetrics.DENSITY_DEFAULT;
            bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), option);
        }
        return bitmap;
    }

    public void downloadversionz(@NonNull final Handler<Integer> handler) {
        final StringRequest request = new StringRequest(Request.Method.GET, DB_VERSION_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                handler.handle(Integer.valueOf(response));
            }
        }, null);
        if (requestQueue == null) {
            final Context context = WsApplication.getContext();
            requestQueue = Volley.newRequestQueue(context);
        }
        requestQueue.add(request);
    }
}