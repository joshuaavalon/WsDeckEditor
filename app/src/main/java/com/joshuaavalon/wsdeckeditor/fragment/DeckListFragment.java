package com.joshuaavalon.wsdeckeditor.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.joshuaavalon.wsdeckeditor.Handler;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.activity.Transactable;
import com.joshuaavalon.wsdeckeditor.fragment.dialog.DeckCreateDialogFragment;
import com.joshuaavalon.wsdeckeditor.fragment.dialog.DeckRenameDialogFragment;
import com.joshuaavalon.wsdeckeditor.model.Deck;
import com.joshuaavalon.wsdeckeditor.repository.DeckRepository;
import com.joshuaavalon.wsdeckeditor.view.AnimatedRecyclerAdapter;
import com.joshuaavalon.wsdeckeditor.view.BaseRecyclerViewHolder;

import java.util.ArrayList;
import java.util.List;

public class DeckListFragment extends BaseFragment implements SearchView.OnQueryTextListener,
        Handler<Void> {
    private RecyclerView recyclerView;
    private DeckListAdapter adapter;
    private List<Deck> decks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_deck_list, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        decks = DeckRepository.getDecks();
        recyclerView.setHasFixedSize(true);
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new DeckListAdapter(decks);
        recyclerView.setAdapter(adapter);

        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(final RecyclerView recyclerView,
                                          final RecyclerView.ViewHolder viewHolder,
                                          final RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(final RecyclerView.ViewHolder viewHolder,
                                         final int direction) {
                        if (!(viewHolder instanceof DeckListViewHolder)) return;
                        final Deck deck = adapter.getDecks().get(viewHolder.getAdapterPosition());
                        DeckRepository.delete(deck);
                        refresh();
                    }
                });
        itemTouchHelper.attachToRecyclerView(recyclerView);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_decklist, menu);
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
        final List<Deck> filteredModelList = filter(decks, query);
        adapter.setModels(filteredModelList);
        recyclerView.scrollToPosition(0);
        return true;
    }

    private List<Deck> filter(List<Deck> models, String query) {
        query = query.toLowerCase();
        final List<Deck> filteredModelList = new ArrayList<>();
        for (Deck model : models) {
            final String text = model.getName().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    @Override
    public void handle(Void object) {
        refresh();
    }

    private void refresh() {
        decks = DeckRepository.getDecks();
        adapter.setModels(decks);
    }

    private class DeckListAdapter extends AnimatedRecyclerAdapter<Deck, DeckListViewHolder> {
        public DeckListAdapter(List<Deck> models) {
            super(models);
        }

        @Override
        public DeckListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_deck, parent, false);
            return new DeckListViewHolder(view);
        }

        @NonNull
        public List<Deck> getDecks() {
            return models;
        }
    }

    private class DeckListViewHolder extends BaseRecyclerViewHolder<Deck> {
        private final TextView textView;
        private final View itemView;
        private final ImageView renameImageView;

        public DeckListViewHolder(final View itemView) {
            super(itemView);
            this.itemView = itemView;
            textView = (TextView) itemView.findViewById(R.id.text_view);
            renameImageView = (ImageView) itemView.findViewById(R.id.rename_button);
        }

        @Override
        public void bind(final Deck deck) {
            textView.setText(deck.getName());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!(getActivity() instanceof Transactable)) return;
                    final Transactable transactable = (Transactable) getActivity();
                    //TODO
                }
            });
            renameImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DeckRenameDialogFragment.start(getFragmentManager(),
                            DeckListFragment.this,
                            deck.getId());
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        final FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        if (fab == null) return;
        fab.show();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeckCreateDialogFragment.start(getFragmentManager(), DeckListFragment.this);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        final FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        if (fab != null)
            fab.hide();
    }
}

