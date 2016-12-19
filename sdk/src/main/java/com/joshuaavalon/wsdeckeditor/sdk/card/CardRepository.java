package com.joshuaavalon.wsdeckeditor.sdk.card;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.webkit.URLUtil;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.joshuaavalon.wsdeckeditor.sdk.BuildConfig;
import com.joshuaavalon.wsdeckeditor.sdk.R;
import com.joshuaavalon.wsdeckeditor.sdk.card.util.ThumbnailUtils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

class CardRepository implements ICardRepository {
    @NonNull
    private final SQLiteDatabase sqLiteDatabase;
    @NonNull
    private final AbstractCardDatabase database;
    @NonNull
    private final Context context;
    @Nullable
    private RequestQueue requestQueue;
    private int recordCount = -1;

    CardRepository(@NonNull final Context context, @NonNull final AbstractCardDatabase database) {
        this.context = context.getApplicationContext();
        this.database = database;
        requestQueue = null;
        sqLiteDatabase = database.getWritableDatabase();
    }

    @NonNull
    private static Card buildCard(@NonNull final Cursor cursor) {
        final Card.Builder builder = new Card.Builder();
        builder.setName(cursor.getString(cursor.getColumnIndexOrThrow(CardScheme.Field.Name)));
        builder.setSerial(cursor.getString(cursor.getColumnIndexOrThrow(CardScheme.Field.Serial)));
        builder.setRarity(cursor.getString(cursor.getColumnIndexOrThrow(CardScheme.Field.Rarity)));
        builder.setExpansion(cursor.getString(cursor.getColumnIndexOrThrow(CardScheme.Field.Expansion)));
        builder.setSide(Card.Side.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(CardScheme.Field.Side))));
        builder.setColor(Card.Color.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(CardScheme.Field.Color))));
        builder.setLevel(cursor.getInt(cursor.getColumnIndexOrThrow(CardScheme.Field.Level)));
        builder.setPower(cursor.getInt(cursor.getColumnIndexOrThrow(CardScheme.Field.Power)));
        builder.setCost(cursor.getInt(cursor.getColumnIndexOrThrow(CardScheme.Field.Cost)));
        builder.setSoul(cursor.getInt(cursor.getColumnIndexOrThrow(CardScheme.Field.Soul)));
        builder.setType(Card.Type.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(CardScheme.Field.Type))));
        builder.setAttribute1(cursor.getString(cursor.getColumnIndexOrThrow(CardScheme.Field.FirstChara)));
        builder.setAttribute2(cursor.getString(cursor.getColumnIndexOrThrow(CardScheme.Field.SecondChara)));
        builder.setText(cursor.getString(cursor.getColumnIndexOrThrow(CardScheme.Field.Text)));
        builder.setFlavor(cursor.getString(cursor.getColumnIndexOrThrow(CardScheme.Field.Flavor)));
        builder.setTrigger(Card.Trigger.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(CardScheme.Field.Trigger))));
        builder.setImage(URLUtil.guessFileName(cursor.getString(cursor.getColumnIndexOrThrow(CardScheme.Field.Image))
                , null, null));
        return builder.build();
    }

    private static void addRange(@NonNull final List<String> selects, @NonNull final List<String> selectArgs,
                                 @NonNull final String table, @Nullable final Filter.Range range) {
        if (range == null) return;
        if (range.getMin() >= 0 && range.getMax() >= 0) {
            selects.add(String.format("%s >= ? AND %s <= ?", table, table));
            selectArgs.add(String.valueOf(range.getMin()));
            selectArgs.add(String.valueOf(range.getMax()));
        } else if (range.getMin() >= 0) {
            selects.add(String.format("%s >= ?", table));
            selectArgs.add(String.valueOf(range.getMin()));
        } else if (range.getMax() >= 0) {
            selects.add(String.format("%s <= ?", table));
            selectArgs.add(String.valueOf(range.getMax()));
        }
    }

    @Override
    public int version() {
        final Cursor cursor = sqLiteDatabase.query(CardScheme.Table.Version,
                new String[]{CardScheme.Field.Version},
                null, null, null, null, null);
        int result = 0;
        if (cursor.moveToFirst())
            result = cursor.getInt(0);
        cursor.close();
        return result;
    }

    @Nullable
    @Override
    public Card random() {
        Cursor cursor;
        if (recordCount < 0) {
            cursor = sqLiteDatabase.query(CardScheme.Table.Card, new String[]{"COUNT(*)"},
                    String.format("%s NOT IN (?,?,?,?)", CardScheme.Field.Rarity),
                    new String[]{"SR", "SP", "RRR", "XR"},
                    null, null, null);
            if (cursor.moveToFirst())
                recordCount = cursor.getInt(0);
            cursor.close();
        }
        Card card = null;
        long id = -1;
        if (recordCount > 0) {
            final long filterId = ThreadLocalRandom.current().nextLong(recordCount) + 1;
            cursor = sqLiteDatabase.query(CardScheme.Table.Card, new String[]{"_id"},
                    String.format("%s NOT IN (?,?,?,?)", CardScheme.Field.Rarity),
                    new String[]{"SR", "SP", "RRR", "XR"},
                    null, null, null, filterId + "," + 1);
            if (cursor.moveToFirst())
                id = cursor.getInt(0);
            cursor.close();
        }
        if (id > 0) {
            cursor = sqLiteDatabase.query(CardScheme.Table.Card, null,
                    "_id = ?", new String[]{String.valueOf(id)}, null, null, null);
            if (cursor.moveToFirst())
                card = buildCard(cursor);
            cursor.close();
        }
        return card;
    }

    @Nullable
    public Card find(@NonNull final String serial) {
        final Cursor cursor = sqLiteDatabase.query(CardScheme.Table.Card, null,
                String.format("%s = ?", CardScheme.Field.Serial),
                new String[]{serial}, null, null, null);
        Card card = null;
        if (cursor.moveToFirst())
            card = buildCard(cursor);
        cursor.close();
        return card;
    }

    @NonNull
    @Override
    public List<Card> findAll(@NonNull final List<String> serials) {
        final String[] selectArgs = Iterables.toArray(serials, String.class);
        final String[] argsPlaceHolder = new String[selectArgs.length];
        Arrays.fill(argsPlaceHolder, "?");
        final String argsPart = Joiner.on(",").join(argsPlaceHolder);
        final List<Card> result = new ArrayList<>();
        final Cursor cursor = sqLiteDatabase.query(CardScheme.Table.Card, null,
                String.format("%s IN (%s)", CardScheme.Field.Serial, argsPart),
                selectArgs, null, null, null);
        if (cursor.moveToFirst())
            do {
                result.add(buildCard(cursor));
            } while (cursor.moveToNext());
        cursor.close();
        return result;
    }

    @NonNull
    @Override
    public Bitmap imageOf(@Nullable Card card) {
        Bitmap bitmap;
        if (card != null) {
            bitmap = getImage(card.getImage());
            if (bitmap == null)
                bitmap = BitmapFactory.decodeResource(context.getResources(),
                        card.getType() != Card.Type.Climax ? R.drawable.dc_w00_00 : R.drawable.dc_w00_000, null);
        } else
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.dc_w00_00, null);
        return bitmap;
    }

    @NonNull
    @Override
    public Bitmap thumbnailOf(@NonNull final Bitmap bitmap) {
        final int dimension = context.getResources()
                .getDimensionPixelSize(R.dimen.thumbnail_size);
        return ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension);
    }

    @Override
    public void updateDatabase(@NonNull InputStream in) {
        database.copyDatabase(in);
        recordCount = -1;
    }

    @NonNull
    @Override
    public List<Card> findAll(@NonNull final Filter filter, final int limit, final int offset) {
        final List<Card> result = new ArrayList<>();
        final List<String> selects = new ArrayList<>();
        final List<String> selectArgs = new ArrayList<>();
        prepareArgument(filter, selects, selectArgs);
        String limitArg = null;
        if (limit >= 0 && offset >= 0)
            limitArg = offset + "," + limit;
        final Cursor cursor = sqLiteDatabase.query(CardScheme.Table.Card, null,
                Joiner.on(" AND ").join(selects),
                selectArgs.toArray(new String[selectArgs.size()]), null, null, null, limitArg);
        if (cursor.moveToFirst())
            do {
                result.add(buildCard(cursor));
            } while (cursor.moveToNext());
        cursor.close();
        return result;
    }

    @NonNull
    @Override
    public List<String> imageUrls() {
        final Cursor cursor = sqLiteDatabase.query(true, CardScheme.Table.Card,
                new String[]{CardScheme.Field.Image},
                null, null, null, null, null, null);
        final List<String> result = new ArrayList<>();
        if (!cursor.moveToFirst()) return result;
        do {
            result.add(cursor.getString(0));
        } while (cursor.moveToNext());
        cursor.close();
        return result;
    }

    @NonNull
    @Override
    public List<String> expansions() {
        final Cursor cursor = sqLiteDatabase.query(true, CardScheme.Table.Card,
                new String[]{CardScheme.Field.Expansion},
                null, null, null, null, null, null);
        final List<String> result = new ArrayList<>();
        if (cursor.moveToFirst())
            do {
                result.add(cursor.getString(0));
            } while (cursor.moveToNext());
        cursor.close();
        return result;
    }

    @Override
    public List<String> keywords(@NonNull final String query, int limit) {
        final Set<String> result = new HashSet<>();
        result.addAll(keywordsByField(query, CardScheme.Field.Serial, limit));
        if (result.size() < limit)
            result.addAll(keywordsByField(query, CardScheme.Field.Name, limit));
        if (result.size() < limit)
            result.addAll(keywordsByField(query, CardScheme.Field.FirstChara, limit));
        if (result.size() < limit)
            result.addAll(keywordsByField(query, CardScheme.Field.SecondChara, limit));
        return Lists.newArrayList(Iterables.limit(result, limit));
    }

    @Override
    public void networkVersion(@NonNull final Response.Listener<Integer> listener, @Nullable final Response.ErrorListener errorListener) {
        getRequestQueue().add(new StringRequest(Request.Method.GET, BuildConfig.versionUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                listener.onResponse(Integer.valueOf(response));
            }
        }, errorListener));
    }

    @Override
    public void needUpdated(@NonNull final Response.Listener<Boolean> listener, @Nullable final Response.ErrorListener errorListener) {
        networkVersion(new Response.Listener<Integer>() {
            @Override
            public void onResponse(Integer response) {
                listener.onResponse(response > version());
            }
        }, errorListener);
    }

    @Override
    public void reset() {
        database.copyDatabase();
    }

    @Nullable
    private Bitmap getImage(@NonNull final String imageName) {
        Bitmap bitmap = null;
        final File image = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), imageName);
        if (image.exists()) {
            final BitmapFactory.Options option = new BitmapFactory.Options();
            option.inDensity = DisplayMetrics.DENSITY_DEFAULT;
            bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), option);
        }
        return bitmap;
    }

    private List<String> keywordsByField(@NonNull final String query, @NonNull final String field,
                                         final int limit) {
        final Cursor cursor = sqLiteDatabase.query(true, CardScheme.Table.Card,
                new String[]{field}, String.format("%s LIKE ?", field), new String[]{query},
                null, null, "_id DESC", String.valueOf(limit));
        final List<String> result = new ArrayList<>();
        if (cursor.moveToFirst())
            do {
                result.add(cursor.getString(0));
            } while (cursor.moveToNext());
        cursor.close();
        return result;
    }

    private void prepareArgument(@NonNull final Filter filter, @NonNull final List<String> selects,
                                 @NonNull final List<String> selectArgs) {
        final String likeSql = "%s LIKE ?";
        final String equalSql = "%s = ?";
        final List<String> keywords = new ArrayList<>();
        if (filter.isHasChara() || filter.isHasName() || filter.isHasSerial() || filter.isHasText())
            for (String keyword : filter.getKeyword()) {
                keywords.clear();
                final String wildCardKeyword = "%" + keyword + "%";
                if (filter.isHasChara()) {
                    keywords.add(String.format(likeSql, CardScheme.Field.FirstChara));
                    selectArgs.add(wildCardKeyword);
                    keywords.add(String.format(likeSql, CardScheme.Field.SecondChara));
                    selectArgs.add(wildCardKeyword);
                }
                if (filter.isHasName()) {
                    keywords.add(String.format(likeSql, CardScheme.Field.Name));
                    selectArgs.add(wildCardKeyword);
                }
                if (filter.isHasSerial()) {
                    keywords.add(String.format(likeSql, CardScheme.Field.Serial));
                    selectArgs.add(wildCardKeyword);
                }
                if (filter.isHasText()) {
                    keywords.add(String.format(likeSql, CardScheme.Field.Text));
                    selectArgs.add(wildCardKeyword);
                }
                selects.add("(" + Joiner.on(" OR ").join(keywords) + ")");
            }
        if (filter.getType() != null) {
            selects.add(String.format(equalSql, CardScheme.Field.Type));
            selectArgs.add(filter.getType().toString());
        }
        if (filter.getTrigger() != null) {
            selects.add(String.format(equalSql, CardScheme.Field.Trigger));
            selectArgs.add(filter.getTrigger().toString());
        }
        if (filter.getColor() != null) {
            selects.add(String.format(equalSql, CardScheme.Field.Color));
            selectArgs.add(filter.getColor().toString());
        }
        if (!TextUtils.isEmpty(filter.getExpansion())) {
            selects.add(String.format(equalSql, CardScheme.Field.Expansion));
            selectArgs.add(filter.getExpansion());
        }
        addRange(selects, selectArgs, CardScheme.Field.Level, filter.getLevel());
        addRange(selects, selectArgs, CardScheme.Field.Cost, filter.getCost());
        addRange(selects, selectArgs, CardScheme.Field.Power, filter.getPower());
        addRange(selects, selectArgs, CardScheme.Field.Soul, filter.getSoul());
        if (filter.isNormalOnly()) {
            selects.add(String.format("%s NOT IN (?,?,?,?)", CardScheme.Field.Rarity));
            selectArgs.add("SR");
            selectArgs.add("SP");
            selectArgs.add("RRR");
            selectArgs.add("XR");
            // Because bushiroad website's error for incorrect rarity
            selects.add(String.format("%s NOT REGEXP '.*([SsRr]|SP|sp)$'", CardScheme.Field.Serial));
        }
    }

    @NonNull
    private RequestQueue getRequestQueue() {
        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(context);
        return requestQueue;
    }
}
