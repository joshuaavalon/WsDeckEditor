package com.joshuaavalon.wsdeckeditor.sdk;


import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.webkit.URLUtil;

import com.google.common.collect.ComparisonChain;

public class Card implements Comparable<Card>, Parcelable {
    @NonNull
    private final String name, serial, rarity, expansion, attribute1, attribute2, text, flavor, image;
    @IntRange(from = 0)
    private final int level, cost, power, soul;
    @NonNull
    private final Side side;
    @NonNull
    private final Type type;
    @NonNull
    private final Color color;
    @NonNull
    private final Trigger trigger;

    private Card(@NonNull final String name, @NonNull final String serial, @NonNull final String rarity,
                 @NonNull final String expansion, @NonNull final Side side, @NonNull final Type type,
                 @NonNull final Color color, @IntRange(from = 0) final int level, @IntRange(from = 0) final int cost,
                 @IntRange(from = 0) final int power, @IntRange(from = 0) final int soul, @NonNull final Trigger trigger,
                 @NonNull final String attribute1, @NonNull final String attribute2, @NonNull final String text,
                 @NonNull final String flavor, @NonNull final String image) {
        this.name = name;
        this.serial = serial;
        this.rarity = rarity;
        this.expansion = expansion;
        this.side = side;
        this.type = type;
        this.color = color;
        this.level = level;
        this.cost = cost;
        this.power = power;
        this.soul = soul;
        this.trigger = trigger;
        this.attribute1 = attribute1;
        this.attribute2 = attribute2;
        this.text = text;
        this.flavor = flavor;
        this.image = image;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getSerial() {
        return serial;
    }

    @NonNull
    public String getRarity() {
        return rarity;
    }

    @NonNull
    public String getExpansion() {
        return expansion;
    }

    @NonNull
    public String getAttribute1() {
        return attribute1;
    }

    @NonNull
    public String getAttribute2() {
        return attribute2;
    }

    @NonNull
    public String getText() {
        return text;
    }

    @NonNull
    public String getFlavor() {
        return flavor;
    }

    @NonNull
    public String getImage() {
        return image;
    }

    public int getLevel() {
        return level;
    }

    public int getCost() {
        return cost;
    }

    public int getPower() {
        return power;
    }

    public int getSoul() {
        return soul;
    }

    @NonNull
    public Side getSide() {
        return side;
    }

    @NonNull
    public Type getType() {
        return type;
    }

    @NonNull
    public Color getColor() {
        return color;
    }

    @NonNull
    public Trigger getTrigger() {
        return trigger;
    }

    @Override
    public int hashCode() {
        return serial.hashCode();
    }

    @Override
    public int compareTo(@NonNull final Card other) {
        return ComparisonChain.start()
                .compare(serial, other.serial)
                .compare(name, other.name)
                .result();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Card && (obj == this || compareTo((Card) obj) == 0);
    }

    public enum Type {
        Character(R.string.type_chara),
        Event(R.string.type_event),
        Climax(R.string.type_climax);
        @StringRes
        private final int resId;

        Type(@StringRes final int resId) {
            this.resId = resId;
        }

        @StringRes
        public int getStringId() {
            return resId;
        }
    }

    public enum Trigger {
        None(R.string.trigger_none),
        OneSoul(R.string.trigger_1s),
        TwoSoul(R.string.trigger_2s),
        Wind(R.string.trigger_wind),
        Fire(R.string.trigger_fire),
        Bag(R.string.trigger_bag),
        Gold(R.string.trigger_gold),
        Door(R.string.trigger_door),
        Book(R.string.trigger_book),
        Gate(R.string.trigger_gate);
        @StringRes
        private final int resId;

        Trigger(@StringRes final int resId) {
            this.resId = resId;
        }

        @StringRes
        public int getStringId() {
            return resId;
        }
    }

    public enum Side {
        W(R.drawable.side_w, R.string.side_w), S(R.drawable.side_s, R.string.side_s);
        @StringRes
        private final int resId;
        @DrawableRes
        private final int drawable;

        Side(@DrawableRes final int drawable, @StringRes final int resId) {
            this.drawable = drawable;
            this.resId = resId;
        }

        @DrawableRes
        public int getDrawableId() {
            return drawable;
        }

        @StringRes
        public int getStringId() {
            return resId;
        }
    }

    public enum Color {
        Yellow(R.string.color_yellow, R.color.card_yellow),
        Green(R.string.color_green, R.color.card_green),
        Red(R.string.color_red, R.color.card_red),
        Blue(R.string.color_blue, R.color.card_blue);
        @StringRes
        private final int resId;
        @ColorRes
        private final int colorResId;

        Color(@StringRes final int resId, @ColorRes final int colorResId) {
            this.resId = resId;
            this.colorResId = colorResId;
        }

        @StringRes
        public int getStringId() {
            return resId;
        }

        @ColorRes
        public int getColorId() {
            return colorResId;
        }
    }

    public static class Builder {
        @NonNull
        private String name, serial, rarity, expansion, attribute1, attribute2, text, flavor, image;
        @IntRange(from = 0)
        private int level, cost, power, soul;
        @NonNull
        private Side side;
        @NonNull
        private Type type;
        @NonNull
        private Color color;
        @NonNull
        private Trigger trigger;

        public Builder() {
            name = "";
            serial = "";
            rarity = "";
            expansion = "";
            side = Side.W;
            type = Type.Character;
            color = Color.Yellow;
            level = 0;
            cost = 0;
            power = 0;
            soul = 0;
            trigger = Trigger.None;
            attribute1 = "";
            attribute2 = "";
            text = "";
            flavor = "";
            image = "";
        }

        @NonNull
        public Builder setName(@NonNull final String name) {
            this.name = name;
            return this;
        }

        @NonNull
        public Builder setSerial(@NonNull final String serial) {
            this.serial = serial;
            return this;
        }

        @NonNull
        public Builder setRarity(@NonNull final String rarity) {
            this.rarity = rarity;
            return this;
        }

        @NonNull
        public Builder setExpansion(@NonNull final String expansion) {
            this.expansion = expansion;
            return this;
        }

        @NonNull
        public Builder setSide(@NonNull Side side) {
            this.side = side;
            return this;
        }

        @NonNull
        public Builder setType(@NonNull Type type) {
            this.type = type;
            return this;
        }

        @NonNull
        public Builder setColor(@NonNull final Color color) {
            this.color = color;
            return this;
        }

        @NonNull
        public Builder setLevel(final int level) {
            this.level = level;
            return this;
        }

        @NonNull
        public Builder setCost(final int cost) {
            this.cost = cost;
            return this;
        }

        @NonNull
        public Builder setPower(final int power) {
            this.power = power;
            return this;
        }

        @NonNull
        public Builder setSoul(final int soul) {
            this.soul = soul;
            return this;
        }

        @NonNull
        public Builder setTrigger(@NonNull final Trigger trigger) {
            this.trigger = trigger;
            return this;
        }

        @NonNull
        public Builder setAttribute1(@NonNull final String attribute1) {
            this.attribute1 = attribute1;
            return this;
        }

        @NonNull
        public Builder setAttribute2(@NonNull final String attribute2) {
            this.attribute2 = attribute2;
            return this;
        }

        @NonNull
        public Builder setText(@NonNull final String text) {
            this.text = text;
            return this;
        }

        @NonNull
        public Builder setFlavor(@NonNull final String flavor) {
            this.flavor = flavor;
            return this;
        }

        @NonNull
        public Builder setImage(@NonNull final String image) {
            this.image = image;
            return this;
        }

        @NonNull
        public Card build() {
            return new Card(name, serial, rarity, expansion, side, type, color, level, cost,
                    power, soul, trigger, attribute1, attribute2, text, flavor, image);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.serial);
        dest.writeString(this.rarity);
        dest.writeString(this.expansion);
        dest.writeString(this.attribute1);
        dest.writeString(this.attribute2);
        dest.writeString(this.text);
        dest.writeString(this.flavor);
        dest.writeString(this.image);
        dest.writeInt(this.level);
        dest.writeInt(this.cost);
        dest.writeInt(this.power);
        dest.writeInt(this.soul);
        dest.writeInt( this.side.ordinal());
        dest.writeInt(this.type.ordinal());
        dest.writeInt(this.color.ordinal());
        dest.writeInt( this.trigger.ordinal());
    }

    protected Card(Parcel in) {
        this.name = in.readString();
        this.serial = in.readString();
        this.rarity = in.readString();
        this.expansion = in.readString();
        this.attribute1 = in.readString();
        this.attribute2 = in.readString();
        this.text = in.readString();
        this.flavor = in.readString();
        this.image = in.readString();
        this.level = in.readInt();
        this.cost = in.readInt();
        this.power = in.readInt();
        this.soul = in.readInt();
        int tmpSide = in.readInt();
        this.side = Side.values()[tmpSide];
        int tmpType = in.readInt();
        this.type =  Type.values()[tmpType];
        int tmpColor = in.readInt();
        this.color = Color.values()[tmpColor];
        int tmpTrigger = in.readInt();
        this.trigger = Trigger.values()[tmpTrigger];
    }

    public static final Creator<Card> CREATOR = new Creator<Card>() {
        @Override
        public Card createFromParcel(Parcel source) {
            return new Card(source);
        }

        @Override
        public Card[] newArray(int size) {
            return new Card[size];
        }
    };
}
