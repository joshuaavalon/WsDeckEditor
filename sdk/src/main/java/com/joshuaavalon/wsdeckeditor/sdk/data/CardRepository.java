package com.joshuaavalon.wsdeckeditor.sdk.data;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.webkit.URLUtil;

import com.joshuaavalon.wsdeckeditor.sdk.Card;
import com.joshuaavalon.wsdeckeditor.sdk.R;
import com.joshuaavalon.wsdeckeditor.sdk.util.Range;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CardRepository {
    @NonNull
    public static Bitmap getImage(@NonNull final Context context, @Nullable final Card card) {
        Bitmap bitmap;
        if (card != null) {
            bitmap = getImage(context, card.getImage());
            if (bitmap == null)
                bitmap = BitmapFactory.decodeResource(context.getResources(),
                        card.getType() != Card.Type.Climax ? R.drawable.dc_w00_00 : R.drawable.dc_w00_000, null);
        } else
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.dc_w00_00, null);
        return bitmap;
    }

    @Nullable
    private static Bitmap getImage(@NonNull final Context context, @NonNull final String imageName) {
        Bitmap bitmap = null;
        final File image = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), imageName);
        if (image.exists()) {
            final BitmapFactory.Options option = new BitmapFactory.Options();
            option.inDensity = DisplayMetrics.DENSITY_DEFAULT;
            bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), option);
        }
        return bitmap;
    }

    @Nullable
    public static Card getCard(@NonNull final Context context, @NonNull final String serial) {
        final Cursor cursor = new CardDatabase(context).getReadableDatabase()
                .query(CardDatabase.Table.Card, null, String.format("%s = ?", CardDatabase.Field.Serial),
                        new String[]{serial}, null, null, null);
        if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        final Card card = buildCard(cursor);
        cursor.close();
        return card;
    }

    @NonNull
    public static Card buildCard(@NonNull final Cursor cursor) {
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
        builder.setImage(URLUtil.guessFileName(cursor.getString(cursor.getColumnIndexOrThrow(CardDatabase.Field.Image))
                , null, null));
        return builder.build();
    }

    public static class Filter implements Parcelable {
        public static final Creator<Filter> CREATOR = new Creator<Filter>() {
            @Override
            public Filter createFromParcel(Parcel source) {
                return new Filter(source);
            }

            @Override
            public Filter[] newArray(int size) {
                return new Filter[size];
            }
        };
        @NonNull
        private Set<String> keyword;
        private boolean hasName, hasChara, hasText, hasSerial, normalOnly;
        @Nullable
        private Card.Type type;
        @Nullable
        private Card.Trigger trigger;
        @Nullable
        private Card.Color color;
        @Nullable
        private Range level, cost, power, soul;
        @Nullable
        private String expansion;

        public Filter() {
            keyword = new HashSet<>();
        }

        protected Filter(Parcel in) {
            final List<String> keywords = new ArrayList<>();
            in.readStringList(keywords);
            this.keyword = new HashSet<>(keywords);
            this.normalOnly = in.readByte() != 0;
            this.hasName = in.readByte() != 0;
            this.hasChara = in.readByte() != 0;
            this.hasText = in.readByte() != 0;
            this.hasSerial = in.readByte() != 0;
            int tmpType = in.readInt();
            this.type = tmpType == -1 ? null : Card.Type.values()[tmpType];
            int tmpTrigger = in.readInt();
            this.trigger = tmpTrigger == -1 ? null : Card.Trigger.values()[tmpTrigger];
            this.level = in.readParcelable(Range.class.getClassLoader());
            this.cost = in.readParcelable(Range.class.getClassLoader());
            this.power = in.readParcelable(Range.class.getClassLoader());
            this.soul = in.readParcelable(Range.class.getClassLoader());
            this.expansion = in.readString();
        }

        @NonNull
        public Set<String> getKeyword() {
            return keyword;
        }

        public void setKeyword(@NonNull final Set<String> keyword) {
            this.keyword = keyword;
        }

        public boolean isNormalOnly() {
            return normalOnly;
        }

        public void setNormalOnly(boolean normalOnly) {
            this.normalOnly = normalOnly;
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
        public Card.Color getColor() {
            return color;
        }

        public void setColor(@Nullable final Card.Color color) {
            this.color = color;
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

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeStringList(new ArrayList<>(this.keyword));
            dest.writeByte(this.normalOnly ? (byte) 1 : (byte) 0);
            dest.writeByte(this.hasName ? (byte) 1 : (byte) 0);
            dest.writeByte(this.hasChara ? (byte) 1 : (byte) 0);
            dest.writeByte(this.hasText ? (byte) 1 : (byte) 0);
            dest.writeByte(this.hasSerial ? (byte) 1 : (byte) 0);
            dest.writeInt(this.type == null ? -1 : this.type.ordinal());
            dest.writeInt(this.trigger == null ? -1 : this.trigger.ordinal());
            dest.writeParcelable(this.level, flags);
            dest.writeParcelable(this.cost, flags);
            dest.writeParcelable(this.power, flags);
            dest.writeParcelable(this.soul, flags);
            dest.writeString(this.expansion);
        }
    }
}
