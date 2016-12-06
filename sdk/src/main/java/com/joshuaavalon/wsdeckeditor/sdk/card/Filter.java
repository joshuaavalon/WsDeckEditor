package com.joshuaavalon.wsdeckeditor.sdk.card;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Objects;

import java.util.HashSet;
import java.util.Set;

public class Filter {
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

    @Override
    public int hashCode() {
        return Objects.hashCode(keyword, hasName, hasChara, hasText, hasSerial, normalOnly,
                type, trigger, color, expansion, level, cost, power, soul);
    }

    public static class Range {
        private int max;
        private int min;

        public Range() {
            max = -1;
            min = -1;
        }

        public int getMax() {
            return max;
        }

        public void setMax(int max) {
            this.max = max;
        }

        public int getMin() {
            return min;
        }

        public void setMin(int min) {
            this.min = min;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Range)) return false;
            final Range range = (Range) obj;
            return Objects.equal(max, range.max) && Objects.equal(min, range.min);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(max, min);
        }
    }
}