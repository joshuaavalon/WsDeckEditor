package com.joshuaavalon.wsdeckeditor.sdk.data;


import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class CardProvider extends ContentProvider {
    private static final String PROVIDER_NAME = "com.joshuaavalon.wsdeckeditor.sdk.Card";
    private static final String CARD_URL = String.format("content://%s/%s", PROVIDER_NAME, CardDatabase.Table.Card);
    static final Uri CARD_CONTENT_URI = Uri.parse(CARD_URL);
    private static final String VERSION_URL = String.format("content://%s/%s", PROVIDER_NAME, CardDatabase.Table.Version);
    static final Uri VERSION_CONTENT_URI = Uri.parse(VERSION_URL);
    private static final UriMatcher uriMatcher;
    private static final int CODE_CARD = 1;
    private static final int CODE_CARD_ID = 2;
    private static final int CODE_VERSION = 3;
    static final String ARG_LIMIT = "limit";
    static final String ARG_OFFSET = "offset";

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, CardDatabase.Table.Card, CODE_CARD);
        uriMatcher.addURI(PROVIDER_NAME, CardDatabase.Table.Card + "/#", CODE_CARD_ID);
        uriMatcher.addURI(PROVIDER_NAME, CardDatabase.Table.Version, CODE_VERSION);
    }

    private SQLiteDatabase database;

    @Override
    public boolean onCreate() {
        final Context context = getContext();
        if (context == null) return false;
        final CardDatabase cardDatabase = new CardDatabase(context);
        database = cardDatabase.getWritableDatabase();
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        switch (uriMatcher.match(uri)) {
            case CODE_CARD:
                queryBuilder.setTables(CardDatabase.Table.Card);
                break;
            case CODE_CARD_ID:
                queryBuilder.setTables(CardDatabase.Table.Card);
                queryBuilder.appendWhere(CardDatabase.Field.Serial + "=" + uri.getPathSegments().get(1));
                break;
            case CODE_VERSION:
                queryBuilder.setTables(CardDatabase.Table.Version);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        final String limit = uri.getQueryParameter(ARG_LIMIT);
        final String offset = uri.getQueryParameter(ARG_OFFSET);
        Cursor cursor;
        if (limit != null && offset != null) {
            final String limitString = offset + "," + limit;
            cursor = queryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder, limitString);
        } else
            cursor = queryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);
        final Context context = getContext();
        if (context != null)
            cursor.setNotificationUri(context.getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case CODE_CARD:
                return "vnd.android.cursor.dir/vnd.com.joshuaavalon.wsdeckeditor.sdk.Card";
            case CODE_CARD_ID:
                return "vnd.android.cursor.item/vnd.com.joshuaavalon.wsdeckeditor.sdk.Card";
            case CODE_VERSION:
                return "vnd.android.cursor.item/vnd.com.joshuaavalon.wsdeckeditor.sdk.Version";
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
