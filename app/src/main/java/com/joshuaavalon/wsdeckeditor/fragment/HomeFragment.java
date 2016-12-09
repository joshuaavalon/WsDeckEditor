package com.joshuaavalon.wsdeckeditor.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.joshuaavalon.android.view.ContentView;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.activity.ResultActivity;
import com.joshuaavalon.wsdeckeditor.config.CardSuggestionProvider;
import com.joshuaavalon.wsdeckeditor.config.ISuggestionProvider;
import com.joshuaavalon.wsdeckeditor.view.search.AbstractSuggestion;
import com.joshuaavalon.wsdeckeditor.view.search.KeywordSuggestion;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

@ContentView(R.layout.fragment_home)
public class HomeFragment extends BaseFragment implements FloatingSearchView.OnQueryChangeListener,
        FloatingSearchView.OnSearchListener, FloatingSearchView.OnFocusChangeListener,
        SearchSuggestionsAdapter.OnBindSuggestionCallback {
    @BindView(R.id.floating_search_view)
    FloatingSearchView floatingSearchView;
    private ISuggestionProvider cardSuggestionProvider;
    private String lastQuery = "";

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        final TextView textView = ButterKnife.findById(view, R.id.text_view);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        initializeSearchView();
        cardSuggestionProvider = new CardSuggestionProvider(getContext(), getCardRepository());
        return view;
    }

    private void initializeSearchView() {
        floatingSearchView.setOnQueryChangeListener(this);
        floatingSearchView.setOnSearchListener(this);
        floatingSearchView.setOnBindSuggestionCallback(this);
        floatingSearchView.setOnFocusChangeListener(this);
    }

    @NonNull
    @Override
    public String getTitle() {
        return getString(R.string.nav_home);
    }

    @Override
    public void onSearchTextChanged(@NonNull final String oldQuery, @NonNull final String newQuery) {
        if (!TextUtils.isEmpty(oldQuery) && TextUtils.isEmpty(newQuery)) {
            floatingSearchView.clearSuggestions();
            return;
        }
        floatingSearchView.showProgress();
        new AsyncTask<String, Void, List<SearchSuggestion>>() {
            @Override
            protected List<SearchSuggestion> doInBackground(String... query) {
                final List<SearchSuggestion> suggestions = cardSuggestionProvider.history(query[0], 3);
                final List<SearchSuggestion> nonHistorySuggestions = Lists.newArrayList(
                        Iterables.limit(Iterables.filter(cardSuggestionProvider.suggestion(query[0], 5),
                                new Predicate<SearchSuggestion>() {
                                    @Override
                                    public boolean apply(SearchSuggestion input) {
                                        return !suggestions.contains(input);
                                    }
                                }),
                                5 - suggestions.size()));
                suggestions.addAll(nonHistorySuggestions);
                return suggestions;
            }

            @Override
            protected void onPostExecute(List<SearchSuggestion> searchSuggestions) {
                floatingSearchView.swapSuggestions(searchSuggestions);
                floatingSearchView.hideProgress();
            }
        }.execute(newQuery);
    }

    @Override
    public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
        if (getPreference().getEnableQuickSearchHistory())
            cardSuggestionProvider.record(searchSuggestion);
        lastQuery = searchSuggestion.getBody();
        if (searchSuggestion instanceof AbstractSuggestion)
            ResultActivity.start(getContext(), ((AbstractSuggestion) searchSuggestion).toFilter(true),
                    floatingSearchView);
    }

    @Override
    public void onSearchAction(String currentQuery) {
        onSuggestionClicked(new KeywordSuggestion(currentQuery));
    }

    @Override
    public void onFocus() {
        floatingSearchView.swapSuggestions(cardSuggestionProvider.history(3));
    }

    @Override
    public void onFocusCleared() {
        if (floatingSearchView != null)
            floatingSearchView.setSearchBarTitle(lastQuery);
    }

    @Override
    public void onBindSuggestion(View suggestionView, ImageView leftIcon, TextView textView, SearchSuggestion item, int itemPosition) {
        final AbstractSuggestion keywordSuggestion = (AbstractSuggestion) item;
        if (keywordSuggestion.isHistory()) {
            leftIcon.setImageResource(R.drawable.ic_history_black_24dp);
            leftIcon.setAlpha(.36f);
        } else
            leftIcon.setImageBitmap(null);
        textView.setText(item.getBody());
    }
}
