package com.joshuaavalon.wsdeckeditor.sdk.card;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.joshuaavalon.wsdeckeditor.sdk.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

class CardDatabase extends AbstractCardDatabase {
    private static final int VERSION = 1;
    @NonNull
    private final Context context;
    @NonNull
    private final File dbFile;

    public CardDatabase(@NonNull final Context context) {
        super(context, CardFacade.DATABASE_NAME, null, VERSION);
        this.context = context;
        final String path = context.getDatabasePath(CardFacade.DATABASE_NAME).getPath();
        dbFile = new File(path);
    }

    @Override
    void copyDatabase() {
        copyDatabase(context.getResources().openRawResource(R.raw.wsdb));
    }

    @Override
    synchronized void copyDatabase(@NonNull final InputStream in) {
        try {
            final File parentFile = dbFile.getParentFile();
            boolean parentExist = parentFile.exists();
            if (!parentExist)
                parentExist = dbFile.getParentFile().mkdirs();
            if (!parentExist)
                return;
            final FileOutputStream out = new FileOutputStream(dbFile);
            final byte[] buff = new byte[1024];
            int read;
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
            in.close();
            out.close();
        } catch (IOException ignored) {
        }
    }

    @Override
    @NonNull
    public synchronized SQLiteDatabase getWritableDatabase() {
        if (!dbFile.exists())
            copyDatabase();
        return super.getWritableDatabase();
    }

    @Override
    @NonNull
    public synchronized SQLiteDatabase getReadableDatabase() {
        if (!dbFile.exists())
            copyDatabase();
        return super.getReadableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
