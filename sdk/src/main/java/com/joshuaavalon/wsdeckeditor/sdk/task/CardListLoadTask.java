package com.joshuaavalon.wsdeckeditor.sdk.task;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.common.base.Joiner;
import com.joshuaavalon.wsdeckeditor.sdk.Card;
import com.joshuaavalon.wsdeckeditor.sdk.data.CardDatabase;
import com.joshuaavalon.wsdeckeditor.sdk.data.CardRepository;
import com.joshuaavalon.wsdeckeditor.sdk.util.Range;

import java.util.ArrayList;
import java.util.List;

public class CardListLoadTask extends CardLoadTask {
    @NonNull
    private final CardRepository.Filter filter;
    private final int limit;
    private final int offset;

    public CardListLoadTask(@Nullable final CallBack<List<Card>> callBack, @NonNull final CardRepository.Filter filter) {
        this(callBack, filter, -1, -1);
    }

    public CardListLoadTask(@Nullable final CallBack<List<Card>> callBack, @NonNull final CardRepository.Filter filter,
                            final int limit, final int offset) {
        super(callBack);
        this.filter = filter;
        this.limit = limit;
        this.offset = offset;
    }

    private static void addRange(@NonNull final List<String> selects, @NonNull final List<String> selectArgs,
                                 @NonNull final String table, @Nullable final Range range) {
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
    protected List<Card> doInBackground(Context... params) {
        final List<Card> result = new ArrayList<>();
        if (params == null || params.length < 1 || params[0] == null) return result;
        final List<String> selects = new ArrayList<>();
        final List<String> selectArgs = new ArrayList<>();
        prepareArgument(selects, selectArgs);
        final SQLiteDatabase database = new CardDatabase(params[0]).getReadableDatabase();
        String limitArg = null;
        if (limit >= 0 && offset >= 0)
            limitArg = offset + "," + limit;
        final Cursor cursor = database.query(CardDatabase.Table.Card, null,
                Joiner.on(" AND ").join(selects),
                selectArgs.toArray(new String[selectArgs.size()]), null, null, null, limitArg);
        if (cursor.moveToFirst())
            do {
                result.add(buildCard(cursor));
            } while (cursor.moveToNext());
        cursor.close();
        return result;
    }

    private void prepareArgument(@NonNull final List<String> selects, @NonNull final List<String> selectArgs) {
        final String likeSql = "%s LIKE ?";
        final String equalSql = "%s = ?";
        final List<String> keywords = new ArrayList<>();
        if (filter.isHasChara() || filter.isHasName() || filter.isHasSerial() || filter.isHasText())
            for (String keyword : filter.getKeyword()) {
                keywords.clear();
                final String wildCardKeyword = "%" + keyword + "%";
                if (filter.isHasChara()) {
                    keywords.add(String.format(likeSql, CardDatabase.Field.FirstChara));
                    selectArgs.add(wildCardKeyword);
                    keywords.add(String.format(likeSql, CardDatabase.Field.SecondChara));
                    selectArgs.add(wildCardKeyword);
                }
                if (filter.isHasName()) {
                    keywords.add(String.format(likeSql, CardDatabase.Field.Name));
                    selectArgs.add(wildCardKeyword);
                }
                if (filter.isHasSerial()) {
                    keywords.add(String.format(likeSql, CardDatabase.Field.Serial));
                    selectArgs.add(wildCardKeyword);
                }
                if (filter.isHasText()) {
                    keywords.add(String.format(likeSql, CardDatabase.Field.Text));
                    selectArgs.add(wildCardKeyword);
                }
                selects.add("(" + Joiner.on(" OR ").join(keywords) + ")");
            }

        if (filter.getType() != null) {
            selects.add(String.format(equalSql, CardDatabase.Field.Type));
            selectArgs.add(filter.getType().toString());
        }

        if (filter.getTrigger() != null) {
            selects.add(String.format(equalSql, CardDatabase.Field.Trigger));
            selectArgs.add(filter.getTrigger().toString());
        }

        if (filter.getColor() != null) {
            selects.add(String.format(equalSql, CardDatabase.Field.Color));
            selectArgs.add(filter.getColor().toString());
        }

        if (!TextUtils.isEmpty(filter.getExpansion())) {
            selects.add(String.format(equalSql, CardDatabase.Field.Expansion));
            selectArgs.add(filter.getExpansion());
        }
        addRange(selects, selectArgs, CardDatabase.Field.Level, filter.getLevel());
        addRange(selects, selectArgs, CardDatabase.Field.Cost, filter.getCost());
        addRange(selects, selectArgs, CardDatabase.Field.Power, filter.getPower());
        addRange(selects, selectArgs, CardDatabase.Field.Soul, filter.getSoul());
        if (filter.isNormalOnly()) {
            selects.add(String.format("%s NOT IN (?,?,?,?)", CardDatabase.Field.Rarity));
            selectArgs.add("SR");
            selectArgs.add("SP");
            selectArgs.add("RRR");
            selectArgs.add("XR");
        }
    }
}