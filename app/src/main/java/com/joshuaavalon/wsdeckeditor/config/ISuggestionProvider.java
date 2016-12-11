package com.joshuaavalon.wsdeckeditor.config;

import android.support.annotation.NonNull;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

import java.util.List;

public interface ISuggestionProvider {
    void clearHistory();

    @NonNull
    List<SearchSuggestion> history(@NonNull String query, int limit);

    @NonNull
    List<SearchSuggestion> history(int limit);

    @NonNull
    List<SearchSuggestion> suggestion(@NonNull String query, int limit);

    void record(@NonNull SearchSuggestion history);
}
