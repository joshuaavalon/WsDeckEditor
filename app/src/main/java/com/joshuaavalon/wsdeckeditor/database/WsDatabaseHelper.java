package com.joshuaavalon.wsdeckeditor.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.joshuaavalon.wsdeckeditor.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class WsDatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "wsdb.db";
    private static final int VERSION = 1;
    private final Context context;
    private final File dbFile;

    public WsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
        this.context = context;
        String path = context.getDatabasePath(DATABASE_NAME).getPath();
        dbFile = new File(path);
    }

    private void copyDatabase() {
        copyDatabase(context.getResources().openRawResource(R.raw.wsdb));
    }

    public synchronized void copyDatabase(InputStream in) {
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
    public synchronized SQLiteDatabase getWritableDatabase() {
        if (!dbFile.exists()) {
            copyDatabase();
        }
        return super.getWritableDatabase();
    }

    @Override
    public synchronized SQLiteDatabase getReadableDatabase() {
        if (!dbFile.exists()) {
            copyDatabase();
        }
        return super.getReadableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
