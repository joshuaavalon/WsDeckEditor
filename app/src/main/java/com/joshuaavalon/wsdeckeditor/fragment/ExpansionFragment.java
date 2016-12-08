package com.joshuaavalon.wsdeckeditor.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.joshuaavalon.android.view.ContentView;
import com.joshuaavalon.android.view.recyclerview.AnimatedRecyclerAdapter;
import com.joshuaavalon.android.view.recyclerview.SimpleViewHolderFactory;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.view.recycler.viewholder.ExpansionViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

@ContentView(R.layout.fragment_expansion)
public class ExpansionFragment extends BaseFragment implements SearchView.OnQueryTextListener {
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    private AnimatedRecyclerAdapter<String> adapter;
    private List<String> expansions;

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        initializeExpansions();
        setHasOptionsMenu(true);
        return view;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getString(R.string.card_expansion);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_expansion, menu);
        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        final List<String> filteredModelList = filter(expansions, query);
        adapter.setModels(filteredModelList);
        recyclerView.scrollToPosition(0);
        return true;
    }

    private List<String> filter(List<String> models, String query) {
        query = query.toLowerCase();
        final List<String> filteredModelList = new ArrayList<>();
        for (String model : models) {
            final String text = model.toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    private void initializeExpansions() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        expansions = getCardRepository().expansions();
        adapter = new AnimatedRecyclerAdapter<>(expansions,
                new SimpleViewHolderFactory<>(R.layout.list_item_expansion, ExpansionViewHolder.class));
        recyclerView.setAdapter(adapter);
    }
}

