package com.joshuaavalon.wsdeckeditor.sdk.card;

import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.google.common.collect.ComparisonChain;
import com.joshuaavalon.wsdeckeditor.sdk.R;

public class Card implements Comparable<Card> {
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

    private Card(@NonNull Builder builder) {
        this.name = builder.name;
        this.serial = builder.serial;
        this.rarity = builder.rarity;
        this.expansion = builder.expansion;
        this.side = builder.side;
        this.type = builder.type;
        this.color = builder.color;
        this.level = builder.level;
        this.cost = builder.cost;
        this.power = builder.power;
        this.soul = builder.soul;
        this.trigger = builder.trigger;
        this.attribute1 = builder.attribute1;
        this.attribute2 = builder.attribute2;
        this.text = builder.text;
        this.flavor = builder.flavor;
        this.image = builder.image;
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
    public boolean equals(Object obj) {
        return obj instanceof Card && (obj == this || compareTo((Card) obj) == 0);
    }

    @Override
    public int compareTo(@NonNull final Card other) {
        return ComparisonChain.start()
                .compare(serial, other.serial)
                .compare(name, other.name)
                .result();
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
            return new Card(this);
        }
    }
}
