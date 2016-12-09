package com.joshuaavalon.wsdeckeditor.view.search;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.joshuaavalon.wsdeckeditor.sdk.card.Filter;

public abstract class AbstractSuggestion implements SearchSuggestion {
    private boolean isHistory;

    public boolean isHistory() {
        return isHistory;
    }

    public void setHistory(boolean history) {
        isHistory = history;
    }

    public abstract Filter toFilter(boolean isNormalOnly);
}
