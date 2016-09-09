package com.joshuaavalon.wsdeckeditor.model;

import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.google.common.collect.ComparisonChain;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.StringResource;

import java.util.Arrays;
import java.util.Comparator;

public class Card implements Comparable<Card> {
    @NonNull
    private final String name;
    @NonNull
    private final String serial;
    @NonNull
    private final String rarity;
    @NonNull
    private final String expansion;
    @NonNull
    private final Side side;
    @NonNull
    private final Type type;
    @NonNull
    private final Color color;
    @IntRange(from = 0)
    private final int level;
    @IntRange(from = 0)
    private final int cost;
    @IntRange(from = 0)
    private final int power;
    @IntRange(from = 0)
    private final int soul;
    @NonNull
    private final Trigger trigger;
    @NonNull
    private final String attribute1;
    @NonNull
    private final String attribute2;
    @NonNull
    private final String text;
    @NonNull
    private final String flavor;
    @NonNull
    private final String image;

    private Card(@NonNull final String name,
                 @NonNull final String serial,
                 @NonNull final String rarity,
                 @NonNull final String expansion,
                 @NonNull final Side side,
                 @NonNull final Type type,
                 @NonNull final Color color,
                 @IntRange(from = 0) final int level,
                 @IntRange(from = 0) final int cost,
                 @IntRange(from = 0) final int power,
                 @IntRange(from = 0) final int soul,
                 @NonNull final Trigger trigger,
                 @NonNull final String attribute1,
                 @NonNull final String attribute2,
                 @NonNull final String text,
                 @NonNull final String flavor,
                 @NonNull final String image) {
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

    public static Comparator<Card> Comparator(@NonNull final SortOrder order) {
        switch (order) {
            case Serial:
                return new SerialComparator();
            case Level:
                return new LevelComparator();
            case Detail:
                return new DetailComparator();
            default:
                return new DetailComparator();
        }

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
    public Trigger getTrigger() {
        return trigger;
    }

    @NonNull
    public String getAttribute1() {
        return attribute1;
    }

    @NonNull
    public String getAttribute2() {
        return attribute2;
    }

    public boolean containAttribute(@NonNull final String attribute) {
        return attribute.equals(attribute1) || attribute.equals(attribute2);
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Card)) return false;
        final Card card = (Card) o;
        return serial.equals(card.serial);

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

    public enum Type implements StringResource {
        Character(R.string.type_chara),
        Event(R.string.type_event),
        Climax(R.string.type_climax);
        @StringRes
        private final int resId;

        Type(@StringRes final int resId) {
            this.resId = resId;
        }

        @Override
        @StringRes
        public int getResId() {
            return resId;
        }
    }

    public enum Trigger implements StringResource {
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

        @Override
        @StringRes
        public int getResId() {
            return resId;
        }
    }

    public enum Side implements StringResource {
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
        public int getDrawable() {
            return drawable;
        }

        @Override
        @StringRes
        public int getResId() {
            return resId;
        }
    }

    public enum Color implements StringResource {
        Yellow(R.string.color_yellow, R.color.cardYellow),
        Green(R.string.color_green, R.color.cardGreen),
        Red(R.string.color_red, R.color.cardRed),
        Blue(R.string.color_blue, R.color.cardBlue);
        @StringRes
        private final int resId;
        @ColorRes
        private final int colorResId;

        Color(@StringRes final int resId, @ColorRes final int colorResId) {
            this.resId = resId;
            this.colorResId = colorResId;
        }

        @Override
        @StringRes
        public int getResId() {
            return resId;
        }

        @ColorRes
        public int getColorResId() {
            return colorResId;
        }
    }


    public enum SortOrder {
        Serial, Level, Detail;
        private static final SortOrder[] values = SortOrder.values();

        public static SortOrder fromInt(int value) {
            return values[value];
        }

        public int toInt() {
            return Arrays.asList(values).indexOf(this);
        }
    }

    public static class Builder {
        @NonNull
        private String name;
        @NonNull
        private String serial;
        @NonNull
        private String rarity;
        @NonNull
        private String expansion;
        @NonNull
        private Side side;
        @NonNull
        private Type type;
        @NonNull
        private Color color;
        @IntRange(from = 0)
        private int level;
        @IntRange(from = 0)
        private int cost;
        @IntRange(from = 0)
        private int power;
        @IntRange(from = 0)
        private int soul;
        @NonNull
        private Trigger trigger;
        @NonNull
        private String attribute1;
        @NonNull
        private String attribute2;
        @NonNull
        private String text;
        @NonNull
        private String flavor;
        @NonNull
        private String image;

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

    private static final class SerialComparator implements Comparator<Card> {
        @Override
        public int compare(final Card left, final Card right) {
            return ComparisonChain.start()
                    .compare(left.serial, right.serial)
                    .result();
        }
    }

    private static final class LevelComparator implements Comparator<Card> {
        @Override
        public int compare(final Card left, final Card right) {
            return ComparisonChain.start()
                    .compare(left.level, right.level)
                    .compare(left.serial, right.serial)
                    .result();
        }
    }

    private static final class DetailComparator implements Comparator<Card> {
        @Override
        public int compare(final Card left, final Card right) {
            return ComparisonChain.start()
                    .compare(left.color.ordinal(), right.color.ordinal())
                    .compare(left.type.ordinal(), right.type.ordinal())
                    .compare(left.level, right.level)
                    .compare(left.serial, right.serial)
                    .result();
        }
    }
}
