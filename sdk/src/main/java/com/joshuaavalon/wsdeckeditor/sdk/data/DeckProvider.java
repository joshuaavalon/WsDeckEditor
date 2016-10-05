package com.joshuaavalon.wsdeckeditor.sdk.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Locale;

public class DeckProvider extends ContentProvider {
    private static final String PROVIDER_NAME = "com.joshuaavalon.wsdeckeditor.sdk.Deck";
    private static final String DECK_URL = String.format("content://%s/%s", PROVIDER_NAME, DeckDatabase.Table.Deck);
    public static final Uri DECK_CONTENT_URI = Uri.parse(DECK_URL);
    private static final String DECK_RECORD_URL = String.format("content://%s/%s", PROVIDER_NAME, DeckDatabase.Table.DeckRecord);
    public static final Uri DECK_RECORD_CONTENT_URI = Uri.parse(DECK_RECORD_URL);
    private static final UriMatcher uriMatcher;
    private static final int CODE_DECK = 1;
    private static final int CODE_DECK_ID = 2;
    private static final int CODE_DECK_RECORD = 3;
    private static final int CODE_DECK_RECORD_ID = 4;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, DeckDatabase.Table.Deck, CODE_DECK);
        uriMatcher.addURI(PROVIDER_NAME, DeckDatabase.Table.Deck + "/#", CODE_DECK_ID);
        uriMatcher.addURI(PROVIDER_NAME, DeckDatabase.Table.DeckRecord, CODE_DECK_RECORD);
        uriMatcher.addURI(PROVIDER_NAME, DeckDatabase.Table.DeckRecord + "/#", CODE_DECK_RECORD_ID);
    }

    private SQLiteDatabase database;

    @NonNull
    private static String addIdConstraint(@Nullable final String selection,
                                          @NonNull final String field, final long id) {
        return String.format(Locale.getDefault(), "%s = %d", field, id) +
                (!TextUtils.isEmpty(selection) ? String.format(" AND ( %s )", selection) : "");
    }

    @Override
    public boolean onCreate() {
        final Context context = getContext();
        if (context == null) return false;
        final DeckDatabase deckDatabase = new DeckDatabase(context);
        database = deckDatabase.getWritableDatabase();
        return database != null;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        switch (uriMatcher.match(uri)) {
            case CODE_DECK:
                queryBuilder.setTables(DeckDatabase.Table.Deck);
                break;
            case CODE_DECK_ID:
                queryBuilder.setTables(DeckDatabase.Table.Deck);
                queryBuilder.appendWhere(DeckDatabase.Field.Id + "=" + ContentUris.parseId(uri));
                break;
            case CODE_DECK_RECORD:
                queryBuilder.setTables(DeckDatabase.Table.DeckRecord);
                break;
            case CODE_DECK_RECORD_ID:
                queryBuilder.setTables(DeckDatabase.Table.DeckRecord);
                queryBuilder.appendWhere(DeckDatabase.Field.DeckId + "=" + ContentUris.parseId(uri));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        final Cursor cursor = queryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);
        final Context context = getContext();
        if (context != null)
            cursor.setNotificationUri(context.getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case CODE_DECK:
                return "vnd.android.cursor.dir/vnd.com.joshuaavalon.wsdeckeditor.sdk.Deck";
            case CODE_DECK_ID:
                return "vnd.android.cursor.item/vnd.com.joshuaavalon.wsdeckeditor.sdk.Deck";
            case CODE_DECK_RECORD:
                return "vnd.android.cursor.dir/vnd.com.joshuaavalon.wsdeckeditor.sdk.DeckRecord";
            case CODE_DECK_RECORD_ID:
                return "vnd.android.cursor.item/vnd.com.joshuaavalon.wsdeckeditor.sdk.DeckRecord";
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        long rowID;
        Uri resultUri;
        switch (uriMatcher.match(uri)) {
            case CODE_DECK:
                rowID = database.insert(DeckDatabase.Table.Deck, "", values);
                resultUri = ContentUris.withAppendedId(DECK_CONTENT_URI, rowID);
                break;
            case CODE_DECK_RECORD:
                rowID = database.insert(DeckDatabase.Table.DeckRecord, "", values);
                resultUri = ContentUris.withAppendedId(DECK_RECORD_CONTENT_URI, rowID);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (rowID > 0) {
            final Context context = getContext();
            if (context != null)
                context.getContentResolver().notifyChange(resultUri, null);
            return resultUri;
        }
        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int count;
        switch (uriMatcher.match(uri)) {
            case CODE_DECK:
                count = database.delete(DeckDatabase.Table.Deck, selection, selectionArgs);
                break;
            case CODE_DECK_ID:
                count = database.delete(DeckDatabase.Table.Deck,
                        addIdConstraint(selection, DeckDatabase.Field.Id, ContentUris.parseId(uri)),
                        selectionArgs);
                break;
            case CODE_DECK_RECORD:
                count = database.delete(DeckDatabase.Table.DeckRecord, selection, selectionArgs);
                break;
            case CODE_DECK_RECORD_ID:
                count = database.delete(DeckDatabase.Table.DeckRecord,
                        addIdConstraint(selection, DeckDatabase.Field.DeckId, ContentUris.parseId(uri)),
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        final Context context = getContext();
        if (context != null)
            context.getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count;
        switch (uriMatcher.match(uri)) {
            case CODE_DECK:
                count = database.update(DeckDatabase.Table.Deck, values, selection, selectionArgs);
                break;
            case CODE_DECK_ID:
                count = database.update(DeckDatabase.Table.Deck, values,
                        addIdConstraint(selection, DeckDatabase.Field.Id, ContentUris.parseId(uri))
                        , selectionArgs);
                break;
            case CODE_DECK_RECORD:
                count = database.update(DeckDatabase.Table.DeckRecord, values, selection, selectionArgs);
                break;
            case CODE_DECK_RECORD_ID:
                count = database.update(DeckDatabase.Table.DeckRecord, values,
                        addIdConstraint(selection, DeckDatabase.Field.DeckId, ContentUris.parseId(uri)),
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        final Context context = getContext();
        if (context != null)
            context.getContentResolver().notifyChange(uri, null);
        return count;
    }
}
