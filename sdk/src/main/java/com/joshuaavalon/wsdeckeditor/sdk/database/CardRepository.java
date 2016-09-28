package com.joshuaavalon.wsdeckeditor.sdk.database;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.joshuaavalon.wsdeckeditor.sdk.Card;
import com.joshuaavalon.wsdeckeditor.sdk.util.Range;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CardRepository {
    @NonNull
    public static Loader<Cursor> newImageLoader(@NonNull final Context context) {
        return new CursorLoader(context, CardProvider.CARD_CONTENT_URI, new String[]{"Distinct " +
                CardDatabase.Field.Image}, null, null, null);
    }

    @NonNull
    public static Loader<Cursor> newExpansionLoader(@NonNull final Context context) {
        return new CursorLoader(context, CardProvider.CARD_CONTENT_URI, new String[]{"Distinct " +
                CardDatabase.Field.Expansion}, null, null, null);
    }

    @NonNull
    public static Loader<Cursor> newVersionLoader(@NonNull final Context context) {
        return new CursorLoader(context, CardProvider.VERSION_CONTENT_URI, new String[]{CardDatabase.Field.Version},
                null, null, null);
    }

    @NonNull
    public static Loader<Cursor> newCardsLoader(@NonNull final Context context, @NonNull final Iterable<String> serials) {
        return newCardsLoaderBySerial(context, CardProvider.CARD_CONTENT_URI, serials);
    }

    @NonNull
    public static Loader<Cursor> newCardsLoader(@NonNull final Context context, @NonNull final Iterable<String> serials,
                                                final int limit, final int offset) {
        final Uri uri = CardProvider.CARD_CONTENT_URI.buildUpon()
                .appendQueryParameter(CardProvider.ARG_LIMIT, String.valueOf(limit))
                .appendQueryParameter(CardProvider.ARG_OFFSET, String.valueOf(offset))
                .build();
        return newCardsLoaderBySerial(context, uri, serials);
    }

    @NonNull
    private static Loader<Cursor> newCardsLoaderBySerial(@NonNull final Context context,
                                                         @NonNull final Uri uri,
                                                         @NonNull final Iterable<String> serials) {
        final String[] selectArgs = Iterables.toArray(serials, String.class);
        final String[] argsPlaceHolder = new String[selectArgs.length];
        Arrays.fill(argsPlaceHolder, "?");
        final String argsPart = Joiner.on(",").join(argsPlaceHolder);
        return new CursorLoader(context, uri, null, String.format("%s IN (%s)", CardDatabase.Field.Serial, argsPart),
                selectArgs, CardDatabase.Field.Serial);
    }

    @NonNull
    public static Loader<Cursor> newCardsLoader(@NonNull final Context context, @NonNull final String expansion,
                                                final int limit, final int offset) {

        final Uri uri = CardProvider.CARD_CONTENT_URI.buildUpon()
                .appendQueryParameter(CardProvider.ARG_LIMIT, String.valueOf(limit))
                .appendQueryParameter(CardProvider.ARG_OFFSET, String.valueOf(offset))
                .build();
        return newCardsLoaderByExpansion(context, uri, expansion);
    }

    @NonNull
    public static Loader<Cursor> newCardsLoader(@NonNull final Context context, @NonNull final String expansion) {
        return newCardsLoaderByExpansion(context, CardProvider.CARD_CONTENT_URI, expansion);
    }

    @NonNull
    private static Loader<Cursor> newCardsLoaderByExpansion(@NonNull final Context context,
                                                            @NonNull final Uri uri,
                                                            @NonNull final String expansion) {
        return new CursorLoader(context, uri, null,
                String.format("%s = ?", CardDatabase.Field.Expansion), new String[]{expansion}, null);
    }

    @NonNull
    public static Loader<Cursor> newCardsLoader(@NonNull final Context context, @NonNull final Filter filter) {
        return newCardsLoaderByFilter(context, CardProvider.CARD_CONTENT_URI, filter);
    }


    @NonNull
    public static Loader<Cursor> newCardsLoader(@NonNull final Context context, @NonNull final Filter filter,
                                                final int limit, final int offset) {
        final Uri uri = CardProvider.CARD_CONTENT_URI.buildUpon()
                .appendQueryParameter(CardProvider.ARG_LIMIT, String.valueOf(limit))
                .appendQueryParameter(CardProvider.ARG_OFFSET, String.valueOf(offset))
                .build();
        return newCardsLoaderByFilter(context, uri, filter);
    }

    @NonNull
    private static Loader<Cursor> newCardsLoaderByFilter(@NonNull final Context context,
                                                         @NonNull final Uri uri,
                                                         @NonNull final Filter filter) {
        final List<String> selects = new ArrayList<>();
        final List<String> selectArgs = new ArrayList<>();
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
                if (filter.isHasName()) {
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

        if (!TextUtils.isEmpty(filter.getExpansion())) {
            selects.add(String.format(equalSql, CardDatabase.Field.Expansion));
            selectArgs.add(filter.getExpansion());
        }
        addRange(selects, selectArgs, CardDatabase.Field.Level, filter.getLevel());
        addRange(selects, selectArgs, CardDatabase.Field.Cost, filter.getCost());
        addRange(selects, selectArgs, CardDatabase.Field.Power, filter.getPower());
        addRange(selects, selectArgs, CardDatabase.Field.Soul, filter.getSoul());
        return new CursorLoader(context, uri, null,
                Joiner.on(" AND ").join(selects), selectArgs.toArray(new String[selectArgs.size()]), null);
    }

    private static void addRange(@NonNull final List<String> selects, @NonNull final List<String> selectArgs,
                                 @NonNull final String table, @Nullable final Range range) {
        if (range == null) return;
        if (range.getMin() >= 0 && range.getMax() >= 0) {
            selects.add(String.format(Locale.getDefault(), "%s >= %d AND %s <= %d", table, range.getMin(),
                    table, range.getMax()));
            selectArgs.add(String.valueOf(range.getMin()));
            selectArgs.add(String.valueOf(range.getMax()));
        } else if (range.getMin() >= 0) {
            selects.add(String.format(Locale.getDefault(), "%s >= %d", table, range.getMin()));
            selectArgs.add(String.valueOf(range.getMin()));
        } else if (range.getMax() >= 0) {
            selects.add(String.format(Locale.getDefault(), "%s <= %d", table, range.getMax()));
            selectArgs.add(String.valueOf(range.getMax()));
        }
    }

    @NonNull
    public static Loader<Cursor> newCardLoader(@NonNull final Context context, @NonNull final String serial) {
        return new CursorLoader(context, CardProvider.CARD_CONTENT_URI, null,
                String.format("%s = ?", CardDatabase.Field.Serial), new String[]{serial}, null);
    }

    @NonNull
    private static Card buildCard(@NonNull final Cursor cursor) {
        final Card.Builder builder = new Card.Builder();
        builder.setName(cursor.getString(cursor.getColumnIndexOrThrow(CardDatabase.Field.Name)));
        builder.setSerial(cursor.getString(cursor.getColumnIndexOrThrow(CardDatabase.Field.Serial)));
        builder.setRarity(cursor.getString(cursor.getColumnIndexOrThrow(CardDatabase.Field.Rarity)));
        builder.setExpansion(cursor.getString(cursor.getColumnIndexOrThrow(CardDatabase.Field.Expansion)));
        builder.setSide(Card.Side.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(CardDatabase.Field.Side))));
        builder.setColor(Card.Color.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(CardDatabase.Field.Color))));
        builder.setLevel(cursor.getInt(cursor.getColumnIndexOrThrow(CardDatabase.Field.Level)));
        builder.setPower(cursor.getInt(cursor.getColumnIndexOrThrow(CardDatabase.Field.Power)));
        builder.setCost(cursor.getInt(cursor.getColumnIndexOrThrow(CardDatabase.Field.Cost)));
        builder.setSoul(cursor.getInt(cursor.getColumnIndexOrThrow(CardDatabase.Field.Soul)));
        builder.setType(Card.Type.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(CardDatabase.Field.Type))));
        builder.setAttribute1(cursor.getString(cursor.getColumnIndexOrThrow(CardDatabase.Field.FirstChara)));
        builder.setAttribute2(cursor.getString(cursor.getColumnIndexOrThrow(CardDatabase.Field.SecondChara)));
        builder.setText(cursor.getString(cursor.getColumnIndexOrThrow(CardDatabase.Field.Text)));
        builder.setFlavor(cursor.getString(cursor.getColumnIndexOrThrow(CardDatabase.Field.Flavor)));
        builder.setTrigger(Card.Trigger.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(CardDatabase.Field.Trigger))));
        builder.setImage(Utils.getImageNameFromUrl(cursor.getString(cursor.getColumnIndexOrThrow(CardDatabase.Field.Image))));
        return builder.build();
    }

    @NonNull
    public static List<Card> toCards(@NonNull final Cursor cursor) {
        final List<Card> cards = new ArrayList<>();
        if (cursor.moveToFirst())
            do {
                cards.add(buildCard(cursor));
            } while (cursor.moveToNext());
        return cards;
    }

    @Nullable
    public static Card toCard(@NonNull final Cursor cursor) {
        if (cursor.moveToFirst())
            return buildCard(cursor);
        else
            return null;
    }

    public static int totVersion(@NonNull final Cursor cursor) {
        final int index = cursor.getColumnIndexOrThrow(CardDatabase.Field.Version);
        return cursor.getInt(index);
    }

    public static class Filter {
        @NonNull
        private Set<String> keyword;
        private boolean hasName, hasChara, hasText, hasSerial;
        @Nullable
        private Card.Type type;
        @Nullable
        private Card.Trigger trigger;
        @Nullable
        private Range level, cost, power, soul;
        @Nullable
        private String expansion;

        public Filter() {
            keyword = new HashSet<>();
        }

        @NonNull
        public Set<String> getKeyword() {
            return keyword;
        }

        public void setKeyword(@NonNull final Set<String> keyword) {
            this.keyword = keyword;
        }

        public boolean isHasName() {
            return hasName;
        }

        public void setHasName(final boolean hasName) {
            this.hasName = hasName;
        }

        public boolean isHasChara() {
            return hasChara;
        }

        public void setHasChara(final boolean hasChara) {
            this.hasChara = hasChara;
        }

        public boolean isHasText() {
            return hasText;
        }

        public void setHasText(final boolean hasText) {
            this.hasText = hasText;
        }

        public boolean isHasSerial() {
            return hasSerial;
        }

        public void setHasSerial(final boolean hasSerial) {
            this.hasSerial = hasSerial;
        }

        @Nullable
        public Card.Type getType() {
            return type;
        }

        public void setType(@Nullable final Card.Type type) {
            this.type = type;
        }

        @Nullable
        public Card.Trigger getTrigger() {
            return trigger;
        }

        public void setTrigger(@Nullable final Card.Trigger trigger) {
            this.trigger = trigger;
        }

        @Nullable
        public Range getLevel() {
            return level;
        }

        public void setLevel(@Nullable final Range level) {
            this.level = level;
        }

        @Nullable
        public Range getCost() {
            return cost;
        }

        public void setCost(@Nullable Range cost) {
            this.cost = cost;
        }

        @Nullable
        public Range getPower() {
            return power;
        }

        public void setPower(@Nullable Range power) {
            this.power = power;
        }

        @Nullable
        public Range getSoul() {
            return soul;
        }

        public void setSoul(@Nullable Range soul) {
            this.soul = soul;
        }

        @Nullable
        public String getExpansion() {
            return expansion;
        }

        public void setExpansion(@Nullable final String expansion) {
            this.expansion = expansion;
        }
    }
}
