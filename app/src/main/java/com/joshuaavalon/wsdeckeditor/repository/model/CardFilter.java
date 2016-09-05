package com.joshuaavalon.wsdeckeditor.repository.model;

import android.support.annotation.NonNull;

import com.google.common.base.Optional;
import com.joshuaavalon.fluentquery.Condition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CardFilter {
    @NonNull
    private final List<CardFilterItem> filterItems;

    public CardFilter() {
        filterItems = new ArrayList<>();
    }

    public void addFilterItem(@NonNull final CardFilterItem filterItem) {
        filterItems.add(filterItem);
    }

    public void addFilterItems(@NonNull final Collection<CardFilterItem> filterItems) {
        this.filterItems.addAll(filterItems);
    }

    @NonNull
    public Optional<Condition> getCondition() {
        Condition condition = null;
        for (CardFilterItem filterItem : filterItems) {
            final Optional<Condition> conditionOptional = filterItem.toCondition();
            if (!conditionOptional.isPresent()) continue;
            if (condition == null)
                condition = conditionOptional.get();
            else
                condition = condition.and(conditionOptional.get());
        }
        return Optional.fromNullable(condition);
    }
}
