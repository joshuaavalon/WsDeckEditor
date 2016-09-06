package com.joshuaavalon.wsdeckeditor.repository.model;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.StringResource;
import com.joshuaavalon.wsdeckeditor.repository.CardRepository;

public class CardFilterItemFactory {

    public static CardFilterItem createFilterItem(@NonNull final CardFilterItemType type) {
        switch (type) {
            case Color:
                return new ColorCardFilterItem();
            case Expansion:
                return new ExpansionCardFilterItem();
            case AndKeyword:
                return new KeywordCardFilterItem(false);
            case NotKeyword:
                return new KeywordCardFilterItem(true);
            case Side:
                return new SideCardFilterItem();
            case Trigger:
                return new TriggerCardFilterItem();
            case Type:
                return new TypeCardFilterItem();
            case Level:
                return new RangeCardFilterItem(R.string.card_level, CardRepository.SQL_CARD_LEVEL);
            case Cost:
                return new RangeCardFilterItem(R.string.card_cost, CardRepository.SQL_CARD_COST);
            case Power:
                return new RangeCardFilterItem(R.string.card_power, CardRepository.SQL_CARD_POWER);
            case Soul:
                return new RangeCardFilterItem(R.string.card_soul, CardRepository.SQL_CARD_SOUL);
            case Normal:
                return new NormalCardFilterItem();
            default:
                throw new IllegalArgumentException();
        }
    }

    public enum CardFilterItemType implements StringResource {
        Color(R.string.card_color),
        Expansion(R.string.card_expansion),
        AndKeyword(R.string.and),
        NotKeyword(R.string.not),
        Side(R.string.card_side),
        Trigger(R.string.card_trigger),
        Type(R.string.card_type),
        Level(R.string.card_level),
        Cost(R.string.cost),
        Power(R.string.power),
        Soul(R.string.soul),
        Normal(R.string.card_normal);
        @StringRes
        private final int resId;

        CardFilterItemType(@StringRes final int resId) {
            this.resId = resId;
        }

        @Override
        @StringRes
        public int getResId() {
            return resId;
        }
    }
}
