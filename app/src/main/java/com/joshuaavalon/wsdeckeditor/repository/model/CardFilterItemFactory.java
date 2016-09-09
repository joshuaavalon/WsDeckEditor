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
                return new RangeCardFilterItem(R.string.filter_dialog_level, CardRepository.SQL_CARD_LEVEL);
            case Cost:
                return new RangeCardFilterItem(R.string.filter_dialog_cost, CardRepository.SQL_CARD_COST);
            case Power:
                return new RangeCardFilterItem(R.string.filter_dialog_power, CardRepository.SQL_CARD_POWER);
            case Soul:
                return new RangeCardFilterItem(R.string.filter_dialog_soul, CardRepository.SQL_CARD_SOUL);
            case Normal:
                return new NormalCardFilterItem();
            default:
                throw new IllegalArgumentException();
        }
    }

    public enum CardFilterItemType implements StringResource {
        Color(R.string.filter_color),
        Expansion(R.string.filter_expansion),
        AndKeyword(R.string.filter_and),
        NotKeyword(R.string.filter_not),
        Side(R.string.filter_side),
        Trigger(R.string.filter_trigger),
        Type(R.string.filter_type),
        Level(R.string.filter_level),
        Cost(R.string.filter_cost),
        Power(R.string.filter_power),
        Soul(R.string.filter_soul),
        Normal(R.string.filter_normal_only);
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
