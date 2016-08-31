package com.joshuaavalon.wsdeckeditor.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.TextView;

import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.activity.Transactable;
import com.joshuaavalon.wsdeckeditor.repository.CardRepository;
import com.joshuaavalon.wsdeckeditor.view.AnimatedRecyclerAdapter;
import com.joshuaavalon.wsdeckeditor.view.BaseRecyclerViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ExpansionFragment extends Fragment implements SearchView.OnQueryTextListener {
    private RecyclerView recyclerView;
    private CardListAdapter adapter;
    private List<String> expansions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_expansion, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        expansions = CardRepository.getExpansions();
        recyclerView.setHasFixedSize(true);
        /* TODO
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int dynamicCount = (int) dpWidth / 360;
        int rowCount = dynamicCount > 1 ? dynamicCount : 1;
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), rowCount);
        */
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new CardListAdapter(expansions);
        recyclerView.setAdapter(adapter);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_cardlist, menu);
        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        List<String> newList = new ArrayList<>(expansions);
        switch (id) {
            case R.id.sort_asc:
                Collections.sort(newList, new Comparator<String>() {
                    @Override
                    public int compare(String s, String t) {
                        return s.compareTo(t);
                    }
                });
                adapter.setModels(newList);
                recyclerView.scrollToPosition(0);
                return true;
            case R.id.sort_desc:
                Collections.sort(newList, new Comparator<String>() {
                    @Override
                    public int compare(String s, String t) {
                        return t.compareTo(s);
                    }
                });
                adapter.setModels(newList);
                recyclerView.scrollToPosition(0);
                return true;
            case R.id.sort_restore:
                adapter.setModels(expansions);
                recyclerView.scrollToPosition(0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    private class CardListAdapter extends AnimatedRecyclerAdapter<String, CardListViewHolder> {
        public CardListAdapter(List<String> models) {
            super(models);
        }

        @Override
        public CardListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_expansion, parent, false);
            return new CardListViewHolder(view);
        }
    }

    private class CardListViewHolder extends BaseRecyclerViewHolder<String> {
        private final TextView textView;
        private final View itemView;

        public CardListViewHolder(final View itemView) {
            super(itemView);
            this.itemView = itemView;
            textView = (TextView) itemView.findViewById(R.id.card_text);
        }

        @Override
        public void bind(final String title) {
            textView.setText(title);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!(getActivity() instanceof Transactable)) return;
                    final Transactable transactable = (Transactable) getActivity();
                    final CardRepository.Filter filter = new CardRepository.Filter();
                    filter.setExpansion(title);
                    final CardListFragment fragment = CardListFragment.newInstance(filter);
                    transactable.transactTo(fragment);
                }
            });
        }
    }
}

