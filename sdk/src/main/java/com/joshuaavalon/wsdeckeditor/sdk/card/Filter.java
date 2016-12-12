package com.joshuaavalon.wsdeckeditor.sdk.card;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Objects;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Filter implements Parcelable {
    public static final Parcelable.Creator<Filter> CREATOR = new Parcelable.Creator<Filter>() {
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
        keyword = new HashSet<>();
        final List<String> keywords = new ArrayList<>();
        in.readStringList(keywords);
        keyword.addAll(keywords);
        hasName = in.readByte() != 0;
        hasChara = in.readByte() != 0;
        hasText = in.readByte() != 0;
        hasSerial = in.readByte() != 0;
        normalOnly = in.readByte() != 0;
        int tmpType = in.readInt();
        type = tmpType == -1 ? null : Card.Type.values()[tmpType];
        int tmpTrigger = in.readInt();
        trigger = tmpTrigger == -1 ? null : Card.Trigger.values()[tmpTrigger];
        int tmpColor = in.readInt();
        color = tmpColor == -1 ? null : Card.Color.values()[tmpColor];
        level = in.readParcelable(Range.class.getClassLoader());
        cost = in.readParcelable(Range.class.getClassLoader());
        power = in.readParcelable(Range.class.getClassLoader());
        soul = in.readParcelable(Range.class.getClassLoader());
        expansion = in.readString();
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
    }    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Filter)) return false;
        final Filter filter = (Filter) o;
        return Objects.equal(hasName, filter.hasName) &&
                Objects.equal(hasChara, filter.hasChara) &&
                Objects.equal(hasText, filter.hasText) &&
                Objects.equal(hasSerial, filter.hasSerial) &&
                Objects.equal(normalOnly, filter.normalOnly) &&
                Objects.equal(keyword, filter.keyword) &&
                Objects.equal(type, filter.type) &&
                Objects.equal(trigger, filter.trigger) &&
                Objects.equal(color, filter.color) &&
                Objects.equal(level, filter.level) &&
                Objects.equal(cost, filter.cost) &&
                Objects.equal(power, filter.power) &&
                Objects.equal(soul, filter.soul) &&
                Objects.equal(expansion, filter.expansion);
    }

    public void setExpansion(@Nullable final String expansion) {
        this.expansion = expansion;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(keyword, hasName, hasChara, hasText, hasSerial, normalOnly,
                type, trigger, color, expansion, level, cost, power, soul);
    }

    public static class Range implements Parcelable {
        public static final Creator<Range> CREATOR = new Creator<Range>() {
            @Override
            public Range createFromParcel(Parcel source) {
                return new Range(source);
            }

            @Override
            public Range[] newArray(int size) {
                return new Range[size];
            }
        };
        private int max;
        private int min;

        public Range() {
            max = -1;
            min = -1;
        }

        protected Range(Parcel in) {
            this.max = in.readInt();
            this.min = in.readInt();
        }

        public int getMax() {
            return max;
        }

        public void setMax(int max) {
            this.max = max;
        }

        public int getMin() {
            return min;
        }        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Range)) return false;
            final Range range = (Range) obj;
            return Objects.equal(max, range.max) && Objects.equal(min, range.min);
        }

        public void setMin(int min) {
            this.min = min;
        }        @Override
        public int hashCode() {
            return Objects.hashCode(max, min);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.max);
            dest.writeInt(this.min);
        }




    }    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(new ArrayList<>(keyword));
        dest.writeByte(hasName ? (byte) 1 : (byte) 0);
        dest.writeByte(hasChara ? (byte) 1 : (byte) 0);
        dest.writeByte(hasText ? (byte) 1 : (byte) 0);
        dest.writeByte(hasSerial ? (byte) 1 : (byte) 0);
        dest.writeByte(normalOnly ? (byte) 1 : (byte) 0);
        dest.writeInt(type == null ? -1 : type.ordinal());
        dest.writeInt(trigger == null ? -1 : trigger.ordinal());
        dest.writeInt(color == null ? -1 : color.ordinal());
        dest.writeParcelable(level, flags);
        dest.writeParcelable(cost, flags);
        dest.writeParcelable(power, flags);
        dest.writeParcelable(soul, flags);
        dest.writeString(expansion);
    }




}