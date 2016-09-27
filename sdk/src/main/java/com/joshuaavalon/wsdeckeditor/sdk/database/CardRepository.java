package com.joshuaavalon.wsdeckeditor.sdk.database;


import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.joshuaavalon.wsdeckeditor.sdk.Card;
import com.joshuaavalon.wsdeckeditor.sdk.util.Range;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
        final String[] selectArgs = Iterables.toArray(serials, String.class);
        final String[] argsPlaceHolder = new String[selectArgs.length];
        Arrays.fill(argsPlaceHolder, "?");
        final String argsPart = Joiner.on(",").join(argsPlaceHolder);
        return new CursorLoader(context, CardProvider.VERSION_CONTENT_URI, null,
                String.format("%s IN (%s)", CardDatabase.Field.Serial, argsPart), selectArgs, CardDatabase.Field.Serial);
    }


    @NonNull
    public static Loader<Cursor> newCardsLoader(@NonNull final Context context, @NonNull final String expansion) {
        return new CursorLoader(context, CardProvider.VERSION_CONTENT_URI, null,
                String.format("%s = ?", CardDatabase.Field.Expansion), new String[]{expansion}, null);
    }

    @NonNull
    public static Loader<Cursor> newCardsLoader(@NonNull final Context context, @NonNull final Filter filter) {
        final List<String> selects = new ArrayList<>();
        final List<String> selectArgs = new ArrayList<>();
        if (filter.getKeyword().size() > 0 && (filter.isHasChara() || filter.isHasName() ||
                filter.isHasSerial() || filter.isHasSerial())) {
            //TODO

        }
        return new CursorLoader(context, CardProvider.VERSION_CONTENT_URI, null,
                String.format("%s = ?", CardDatabase.Field.Serial), new String[]{}, null);
    }

    @NonNull
    public static Loader<Cursor> newCardLoader(@NonNull final Context context, @NonNull final String serial) {
        return new CursorLoader(context, CardProvider.VERSION_CONTENT_URI, null,
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

    @NonNull
    public static Optional<Card> toCard(@NonNull final Cursor cursor) {
        if (cursor.moveToFirst())
            return Optional.of(buildCard(cursor));
        else
            return Optional.absent();
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
