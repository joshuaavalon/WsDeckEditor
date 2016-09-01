package com.joshuaavalon.wsdeckeditor.repository;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.common.base.Optional;
import com.joshuaavalon.fluentquery.Condition;
import com.joshuaavalon.fluentquery.Query;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.Utility;
import com.joshuaavalon.wsdeckeditor.WsApplication;
import com.joshuaavalon.wsdeckeditor.database.WsDatabaseHelper;
import com.joshuaavalon.wsdeckeditor.model.Card;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CardRepository {
    private static final String SQL_CARD = "card";
    private static final String SQL_CARD_NAME = "Name";
    private static final String SQL_CARD_SERIAL = "Serial";
    private static final String SQL_CARD_RARITY = "Rarity";
    private static final String SQL_CARD_EXP = "Expansion";
    private static final String SQL_CARD_SIDE = "Side";
    private static final String SQL_CARD_COLOR = "Color";
    private static final String SQL_CARD_LEVEL = "Level";
    private static final String SQL_CARD_POWER = "Power";
    private static final String SQL_CARD_COST = "Cost";
    private static final String SQL_CARD_SOUL = "Soul";
    private static final String SQL_CARD_TYPE = "Type";
    private static final String SQL_CARD_FIRST_CHR = "FirstChara";
    private static final String SQL_CARD_SECOND_CHR = "SecondChara";
    private static final String SQL_CARD_TXT = "Text";
    private static final String SQL_CARD_FLAVOR = "Flavor";
    private static final String SQL_CARD_TRIGGER = "Trigger";
    private static final String SQL_CARD_IMAGE = "Image";
    private static final String SQL_VERSION = "version";
    private static final String SQL_VERSION_FIELD = "Version";

    @Nullable
    private static Condition getCondition(@NonNull final Collection<String> phases,
                                          final boolean enableName,
                                          final boolean enableChara,
                                          final boolean enableText,
                                          final boolean enableSerial) {
        final List<Condition> conditions = new ArrayList<>();
        for (String phase : phases) {
            if (enableName)
                conditions.add(Condition.property(SQL_CARD_NAME).like(phase));
            if (enableChara)
                conditions.add(Condition.property(SQL_CARD_FIRST_CHR).like(phase)
                        .or(Condition.property(SQL_CARD_SECOND_CHR).like(phase))
                );
            if (enableText)
                conditions.add(Condition.property(SQL_CARD_TXT).like(phase));
            if (enableSerial)
                conditions.add(Condition.property(SQL_CARD_SERIAL).like(phase));
        }

        Condition resultCondition = null;
        for (Condition condition : conditions) {
            if (resultCondition == null)
                resultCondition = condition;
            else
                resultCondition = orConditions(resultCondition, condition);
        }
        return resultCondition;
    }

    @NonNull
    private static Condition andConditions(@NonNull final Condition left,
                                           @Nullable final Condition right) {
        if (right == null)
            return left;
        return left.and(right);
    }

    @NonNull
    private static Condition orConditions(@NonNull final Condition left,
                                          @Nullable final Condition right) {
        if (right == null)
            return left;
        return left.or(right);
    }

    @NonNull
    public static List<Card> getCards(@NonNull final Filter filter) {
        final Condition andCondition = getCondition(filter.getAndList(),
                filter.isEnableName(), filter.isEnableChara()
                , filter.isEnableText(), filter.isEnableSerial());

        final Condition orCondition = getCondition(filter.getOrList(),
                filter.isEnableName(), filter.isEnableChara()
                , filter.isEnableText(), filter.isEnableSerial());
        Condition whereCondition;
        if (andCondition != null)
            whereCondition = andConditions(andCondition, orCondition);
        else
            whereCondition = orCondition;

        final Condition notCondition = getCondition(filter.getNotList(),
                filter.isEnableName(), filter.isEnableChara()
                , filter.isEnableText(), filter.isEnableSerial());
        if (notCondition != null)
            whereCondition = andConditions(notCondition.not(), whereCondition);

        if (!filter.getSide().equals(""))
            whereCondition = andConditions(Condition.property(SQL_CARD_SIDE).equal(filter.getSide()), whereCondition);

        if (!filter.getTrigger().equals(""))
            whereCondition = andConditions(Condition.property(SQL_CARD_TRIGGER).equal(filter.getTrigger()), whereCondition);

        if (!filter.getExpansion().equals(""))
            whereCondition = andConditions(Condition.property(SQL_CARD_EXP).equal(filter.getExpansion()), whereCondition);

        if (!filter.getType().equals(""))
            whereCondition = andConditions(Condition.property(SQL_CARD_TYPE).equal(filter.getType()), whereCondition);

        if (!filter.getColor().equals(""))
            whereCondition = andConditions(Condition.property(SQL_CARD_COLOR).equal(filter.getColor()), whereCondition);

        if (filter.getMaxLevel() != -1)
            whereCondition = andConditions(Condition.property(SQL_CARD_LEVEL).lesserThanOrEqual(String.valueOf(filter.getMaxLevel())), whereCondition);

        if (filter.getMinLevel() != -1)
            whereCondition = andConditions(Condition.property(SQL_CARD_LEVEL).greaterThanOrEqual(String.valueOf(filter.getMinLevel())), whereCondition);

        if (filter.getMaxPower() != -1)
            whereCondition = andConditions(Condition.property(SQL_CARD_POWER).lesserThanOrEqual(String.valueOf(filter.getMaxPower())), whereCondition);

        if (filter.getMinPower() != -1)
            whereCondition = andConditions(Condition.property(SQL_CARD_POWER).greaterThanOrEqual(String.valueOf(filter.getMinPower())), whereCondition);

        if (filter.getMaxCost() != -1)
            whereCondition = andConditions(Condition.property(SQL_CARD_COST).lesserThanOrEqual(String.valueOf(filter.getMaxCost())), whereCondition);

        if (filter.getMinCost() != -1)
            whereCondition = andConditions(Condition.property(SQL_CARD_COST).greaterThanOrEqual(String.valueOf(filter.getMinCost())), whereCondition);

        if (filter.getMaxSoul() != -1)
            whereCondition = andConditions(Condition.property(SQL_CARD_SOUL).lesserThanOrEqual(String.valueOf(filter.getMaxSoul())), whereCondition);

        if (filter.getMinSoul() != -1)
            whereCondition = andConditions(Condition.property(SQL_CARD_SOUL).greaterThanOrEqual(String.valueOf(filter.getMinSoul())), whereCondition);
        if (filter.isNormalOnly()) {
            final Condition conditionSR = Condition.property(SQL_CARD_RARITY).equal("SR");
            final Condition conditionSP = Condition.property(SQL_CARD_RARITY).equal("SP");
            final Condition conditionRRR = Condition.property(SQL_CARD_RARITY).equal("RRR");
            whereCondition = andConditions(conditionSR.or(conditionSP.or(conditionRRR)).not(), whereCondition);
        }

        final Query query = Query.select(getCols()).from(SQL_CARD);
        if (whereCondition != null) {
            query.where(whereCondition);
            Log.e("Condition", whereCondition.toString());
        }

        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = query.commit(db);
        final List<Card> cards = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                cards.add(buildCard(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return cards;
    }

    @NonNull
    public static List<String> getAllImages() {
        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = Query.select(SQL_CARD_IMAGE).from(SQL_CARD).commit(db);
        List<String> images = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                images.add(cursor.getString(0));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return images;
    }

    @NonNull
    public static List<String> getExpansions() {
        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = Query.select(SQL_CARD_EXP).from(SQL_CARD).distinct().commit(db);
        List<String> expansions = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                expansions.add(cursor.getString(0));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return expansions;
    }

    @NonNull
    public static Optional<Card> getCardBySerial(@NonNull final String serial) {
        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = Query.select(getCols()).from(SQL_CARD)
                .where(Condition.property(SQL_CARD_SERIAL).equal(serial)).commit(db);
        Card card;
        if (cursor.moveToFirst()) {
            card = buildCard(cursor);
            cursor.close();
        } else
            card = new Card.Builder().build();
        db.close();
        return Optional.fromNullable(card);
    }

    @NonNull
    private static String[] getCols() {
        return new String[]{
                SQL_CARD_NAME,
                SQL_CARD_SERIAL,
                SQL_CARD_RARITY,
                SQL_CARD_EXP,
                SQL_CARD_SIDE,
                SQL_CARD_COLOR,
                SQL_CARD_LEVEL,
                SQL_CARD_POWER,
                SQL_CARD_COST,
                SQL_CARD_SOUL,
                SQL_CARD_TYPE,
                SQL_CARD_FIRST_CHR,
                SQL_CARD_SECOND_CHR,
                SQL_CARD_TXT,
                SQL_CARD_FLAVOR,
                SQL_CARD_TRIGGER,
                SQL_CARD_IMAGE
        };
    }

    @NonNull
    private static Card buildCard(@NonNull final Cursor cursor) {
        final Card.Builder builder = new Card.Builder();
        builder.setName(cursor.getString(cursor.getColumnIndex(SQL_CARD_NAME)));
        builder.setSerial(cursor.getString(cursor.getColumnIndex(SQL_CARD_SERIAL)));
        builder.setRarity(cursor.getString(cursor.getColumnIndex(SQL_CARD_RARITY)));
        builder.setExpansion(cursor.getString(cursor.getColumnIndex(SQL_CARD_EXP)));
        builder.setSide(Card.Side.valueOf(cursor.getString(cursor.getColumnIndex(SQL_CARD_SIDE))));
        builder.setColor(Card.Color.valueOf(cursor.getString(cursor.getColumnIndex(SQL_CARD_COLOR))));
        builder.setLevel(cursor.getInt(cursor.getColumnIndex(SQL_CARD_LEVEL)));
        builder.setPower(cursor.getInt(cursor.getColumnIndex(SQL_CARD_POWER)));
        builder.setCost(cursor.getInt(cursor.getColumnIndex(SQL_CARD_COST)));
        builder.setSoul(cursor.getInt(cursor.getColumnIndex(SQL_CARD_SOUL)));
        builder.setType(Card.Type.valueOf(cursor.getString(cursor.getColumnIndex(SQL_CARD_TYPE))));
        builder.setAttribute1(cursor.getString(cursor.getColumnIndex(SQL_CARD_FIRST_CHR)));
        builder.setAttribute2(cursor.getString(cursor.getColumnIndex(SQL_CARD_SECOND_CHR)));
        builder.setText(cursor.getString(cursor.getColumnIndex(SQL_CARD_TXT)));
        builder.setFlavor(cursor.getString(cursor.getColumnIndex(SQL_CARD_FLAVOR)));
        builder.setTrigger(Card.Trigger.valueOf(cursor.getString(cursor.getColumnIndex(SQL_CARD_TRIGGER))));
        builder.setImage(Utility.getImageNameFromUrl(cursor.getString(cursor.getColumnIndex(SQL_CARD_IMAGE))));
        return builder.build();
    }

    public static int getVersion() {
        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = Query.select(SQL_VERSION_FIELD).from(SQL_VERSION).commit(db);
        int version = -1;
        if (cursor.moveToFirst())
            version = cursor.getInt(0);
        cursor.close();
        return version;
    }

    public static Bitmap getImage(String imageName, Card.Type type) {
        Bitmap bitmap = null;
        final Context context = WsApplication.getContext();
        final File image = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), imageName);
        if (image.exists()) {
            final BitmapFactory.Options option = new BitmapFactory.Options();
            option.inDensity = DisplayMetrics.DENSITY_DEFAULT;
            bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), option);
        }
        if (bitmap == null)
            bitmap = BitmapFactory.decodeResource(context.getResources(),
                    type != Card.Type.Climax ? R.drawable.dc_w00_00 : R.drawable.dc_w00_000, null);
        return bitmap;
    }

    @NonNull
    private static SQLiteDatabase getReadableDatabase() {
        return new WsDatabaseHelper(WsApplication.getContext()).getReadableDatabase();
    }

    public static class Filter implements Parcelable {
        public static final Creator<Filter> CREATOR = new Creator<Filter>() {
            @Override
            public Filter createFromParcel(Parcel in) {
                return new Filter(in);
            }

            @Override
            public Filter[] newArray(int size) {
                return new Filter[size];
            }
        };
        private List<String> andList;
        private List<String> orList;
        private List<String> notList;
        private String type;
        private String side;
        private String color;
        private String expansion;
        private int maxLevel;
        private int minLevel;
        private int maxPower;
        private int minPower;
        private int maxCost;
        private int minCost;
        private int maxSoul;
        private int minSoul;
        private String trigger;
        private boolean enableName;
        private boolean enableSerial;
        private boolean enableChara;
        private boolean enableText;
        private boolean normalOnly;

        public Filter() {
            andList = new ArrayList<>();
            orList = new ArrayList<>();
            notList = new ArrayList<>();
            maxLevel = -1;
            minLevel = -1;
            maxPower = -1;
            minPower = -1;
            maxCost = -1;
            minCost = -1;
            maxSoul = -1;
            minSoul = -1;
            enableName = true;
            enableSerial = true;
            enableChara = true;
            enableText = true;
            normalOnly = false;
            trigger = "";
            side = "";
            color = "";
            expansion = "";
            type = "";
        }

        protected Filter(Parcel in) {
            andList = in.createStringArrayList();
            orList = in.createStringArrayList();
            notList = in.createStringArrayList();
            type = in.readString();
            side = in.readString();
            color = in.readString();
            expansion = in.readString();
            maxLevel = in.readInt();
            minLevel = in.readInt();
            maxPower = in.readInt();
            minPower = in.readInt();
            maxCost = in.readInt();
            minCost = in.readInt();
            maxSoul = in.readInt();
            minSoul = in.readInt();
            trigger = in.readString();
            final boolean[] booleanArray = new boolean[5];
            in.readBooleanArray(booleanArray);
            enableName = booleanArray[0];
            enableSerial = booleanArray[1];
            enableChara = booleanArray[2];
            enableText = booleanArray[3];
            normalOnly = booleanArray[4];
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getExpansion() {
            return expansion;
        }

        public void setExpansion(String expansion) {
            this.expansion = expansion;
        }

        public String getSide() {
            return side;
        }

        public void setSide(String side) {
            this.side = side;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public int getMaxLevel() {
            return maxLevel;
        }

        public void setMaxLevel(int maxLevel) {
            this.maxLevel = maxLevel;
        }

        public int getMinLevel() {
            return minLevel;
        }

        public void setMinLevel(int minLevel) {
            this.minLevel = minLevel;
        }

        public int getMaxPower() {
            return maxPower;
        }

        public void setMaxPower(int maxPower) {
            this.maxPower = maxPower;
        }

        public int getMinPower() {
            return minPower;
        }

        public void setMinPower(int minPower) {
            this.minPower = minPower;
        }

        public int getMaxCost() {
            return maxCost;
        }

        public void setMaxCost(int maxCost) {
            this.maxCost = maxCost;
        }

        public int getMinCost() {
            return minCost;
        }

        public void setMinCost(int minCost) {
            this.minCost = minCost;
        }

        public int getMaxSoul() {
            return maxSoul;
        }

        public void setMaxSoul(int maxSoul) {
            this.maxSoul = maxSoul;
        }

        public int getMinSoul() {
            return minSoul;
        }

        public void setMinSoul(int minSoul) {
            this.minSoul = minSoul;
        }

        public String getTrigger() {
            return trigger;
        }

        public void setTrigger(String trigger) {
            this.trigger = trigger;
        }

        public boolean isEnableName() {
            return enableName;
        }

        public void setEnableName(boolean enableName) {
            this.enableName = enableName;
        }

        public boolean isEnableSerial() {
            return enableSerial;
        }

        public void setEnableSerial(boolean enableSerial) {
            this.enableSerial = enableSerial;
        }

        public boolean isEnableChara() {
            return enableChara;
        }

        public void setEnableChara(boolean enableChara) {
            this.enableChara = enableChara;
        }

        public boolean isEnableText() {
            return enableText;
        }

        public void setEnableText(boolean enableText) {
            this.enableText = enableText;
        }

        public boolean isNormalOnly() {
            return normalOnly;
        }

        public void setNormalOnly(boolean normalOnly) {
            this.normalOnly = normalOnly;
        }

        public void addAnd(String str) {
            andList.add(str);
        }

        public void addAnd(List<String> str) {
            andList.addAll(str);
        }

        public void addOr(String str) {
            orList.add(str);
        }

        public void addOr(List<String> str) {
            orList.addAll(str);
        }

        public void addNot(String str) {
            notList.add(str);
        }

        public void addNot(List<String> str) {
            notList.addAll(str);
        }

        public Collection<String> getAndList() {
            return andList;
        }

        public Collection<String> getOrList() {
            return orList;
        }

        public Collection<String> getNotList() {
            return notList;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeStringList(andList);
            parcel.writeStringList(orList);
            parcel.writeStringList(notList);
            parcel.writeString(type);
            parcel.writeString(side);
            parcel.writeString(color);
            parcel.writeString(expansion);
            parcel.writeInt(maxLevel);
            parcel.writeInt(minLevel);
            parcel.writeInt(maxPower);
            parcel.writeInt(minPower);
            parcel.writeInt(maxCost);
            parcel.writeInt(minCost);
            parcel.writeInt(maxSoul);
            parcel.writeInt(minSoul);
            parcel.writeString(trigger);
            parcel.writeBooleanArray(new boolean[]{enableName,
                    enableSerial,
                    enableChara,
                    enableText,
                    normalOnly});
        }
    }
}
