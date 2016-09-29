package com.joshuaavalon.wsdeckeditor.sdk.data;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.os.ResultReceiver;
import android.util.Log;
import android.webkit.URLUtil;

import com.google.common.collect.Lists;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class DownloadService extends IntentService {
    private static final String ACTION_DOWNLOAD_IMAGE = "com.joshuaavalon.wsdeckeditor.sdk.data.action.DownloadImage";
    private static final String ACTION_DOWNLOAD_DB = "com.joshuaavalon.wsdeckeditor.sdk.data.action.DownloadDatabase";

    private static final String EXTRA_RECEIVER = "com.joshuaavalon.wsdeckeditor.sdk.data.extra.Receiver";
    private static final String EXTRA_REQUEST_CODE = "com.joshuaavalon.wsdeckeditor.sdk.data.extra.RequestCode";
    private static final String EXTRA_URLS = "com.joshuaavalon.wsdeckeditor.sdk.data.extra.Urls";
    private static final String EXTRA_FORCED = "com.joshuaavalon.wsdeckeditor.sdk.data.extra.Forced";

    public static final String ARG_PROGRESS = "com.joshuaavalon.wsdeckeditor.sdk.data.response.Progress";

    public DownloadService() {
        super("DownloadService");
    }

    public static void startDownloadImages(@NonNull final Context context, @NonNull final ResultReceiver receiver,
                                           final int requestCode, @NonNull final Iterable<String> urls,
                                           final boolean forced) {
        final Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_DOWNLOAD_IMAGE);
        intent.putExtra(EXTRA_RECEIVER, receiver);
        intent.putExtra(EXTRA_REQUEST_CODE, requestCode);
        intent.putExtra(EXTRA_URLS, Lists.newArrayList(urls));
        intent.putExtra(EXTRA_FORCED, forced);
        context.startService(intent);
    }

    public static void startDownloadCardDatabase(@NonNull final Context context, @NonNull final ResultReceiver receiver,
                                                 final int requestCode, @NonNull final String url) {
        final Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_DOWNLOAD_DB);
        intent.putExtra(EXTRA_RECEIVER, receiver);
        intent.putExtra(EXTRA_REQUEST_CODE, requestCode);
        intent.putExtra(EXTRA_URLS, url);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable final Intent intent) {
        if (intent == null) return;
        final String action = intent.getAction();
        if (ACTION_DOWNLOAD_IMAGE.equals(action)) {
            final ResultReceiver receiver = intent.getParcelableExtra(EXTRA_RECEIVER);
            final int requestCode = intent.getIntExtra(EXTRA_REQUEST_CODE, 0);
            final ArrayList<String> urls = intent.getStringArrayListExtra(EXTRA_URLS);
            final boolean forced = intent.getBooleanExtra(EXTRA_FORCED, false);
            handleDownloadImages(receiver, requestCode, urls, forced);
        } else if (ACTION_DOWNLOAD_DB.equals(action)) {
            final ResultReceiver receiver = intent.getParcelableExtra(EXTRA_RECEIVER);
            final int requestCode = intent.getIntExtra(EXTRA_REQUEST_CODE, 0);
            final String url = intent.getStringExtra(EXTRA_URLS);
            handleDownloadDatabase(receiver, requestCode, url);
        }
    }

    private void handleDownloadImages(@NonNull final ResultReceiver receiver, final int requestCode,
                                      @NonNull final List<String> urls, final boolean forced) {
        int finishCount = 0;
        for (String urlToDownload : urls) {
            try {
                final URL url = new URL(urlToDownload);
                final URLConnection connection = url.openConnection();
                connection.connect();

                final InputStream input = new BufferedInputStream(connection.getInputStream());
                final File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        URLUtil.guessFileName(urlToDownload, null, null));
                final OutputStream output = new FileOutputStream(file);

                final byte data[] = new byte[1024];
                int count;
                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                finishCount++;
            }
            final Bundle resultData = new Bundle();
            resultData.putInt(ARG_PROGRESS, finishCount);
            receiver.send(requestCode, resultData);
        }
    }


    private void handleDownloadDatabase(@NonNull final ResultReceiver receiver, final int requestCode,
                                        @NonNull final String urlToDownload) {
        try {
            final URL url = new URL(urlToDownload);
            final URLConnection connection = url.openConnection();
            connection.connect();

            final InputStream input = new BufferedInputStream(connection.getInputStream());
            final File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                    CardDatabase.DATABASE_NAME);
            final OutputStream output = new FileOutputStream(file);
            final int fileLength = connection.getContentLength();
            final byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                final Bundle resultData = new Bundle();
                resultData.putInt(ARG_PROGRESS, (int) (total * 100 / fileLength));
                receiver.send(requestCode, resultData);
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
            final CardDatabase database = new CardDatabase(getApplicationContext());
            try {
                database.copyDatabase(new FileInputStream(file));
            } catch (FileNotFoundException ignored) {
            }
            if (!file.delete())
                Log.e("DownloadService", "Failed to clear db cache");
        } catch (IOException e) {
            e.printStackTrace();
        }
        final Bundle resultData = new Bundle();
        resultData.putInt(ARG_PROGRESS, 100);
        receiver.send(requestCode, resultData);
    }
}
