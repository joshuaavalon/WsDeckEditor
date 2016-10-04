package com.joshuaavalon.wsdeckeditor;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
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

import com.joshuaavalon.wsdeckeditor.sdk.data.DeckRepository;
import com.joshuaavalon.wsdeckeditor.sdk.util.AbstractDeck;
import com.joshuaavalon.wsdeckeditor.view.AnimatedRecyclerAdapter;
import com.joshuaavalon.wsdeckeditor.view.BaseRecyclerViewHolder;

import java.util.ArrayList;
import java.util.List;

public class DeckListFragment extends BaseFragment implements SearchView.OnQueryTextListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    private RecyclerView recyclerView;
    private DeckListAdapter adapter;
    private List<AbstractDeck> decks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_expansion, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        decks = new ArrayList<>();
        adapter = new DeckListAdapter(new ArrayList<>(decks));
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
                        final AbstractDeck deck = adapter.getModels().get(viewHolder.getAdapterPosition());
                        DeckRepository.deleteDeck(getContext(), deck.getId());
                    }
                });
        itemTouchHelper.attachToRecyclerView(recyclerView);
        setHasOptionsMenu(true);
        getActivity().getSupportLoaderManager().initLoader(LoaderId.DeckListLoader, null, this);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_deck, menu);
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
        final List<AbstractDeck> filteredModelList = filter(decks, query);
        adapter.setModels(filteredModelList);
        recyclerView.scrollToPosition(0);
        return true;
    }

    private List<AbstractDeck> filter(List<AbstractDeck> models, String query) {
        query = query.toLowerCase();
        final List<AbstractDeck> filteredModelList = new ArrayList<>();
        for (AbstractDeck model : models) {
            final String text = model.getName().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return DeckRepository.newDecksLoader(getContext());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        decks = DeckRepository.toDecks(data);
        adapter.setModels(new ArrayList<>(decks));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //no-ops
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.action_add:
                DialogUtils.showCreateDeckDialog(getContext());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class DeckListAdapter extends AnimatedRecyclerAdapter<AbstractDeck, DeckListViewHolder> {
        public DeckListAdapter(List<AbstractDeck> models) {
            super(models);
        }

        @Override
        public DeckListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_deck, parent, false);
            return new DeckListViewHolder(view);
        }

        @NonNull
        public List<AbstractDeck> getModels() {
            return models;
        }
    }

    private class DeckListViewHolder extends BaseRecyclerViewHolder<AbstractDeck> {
        private final TextView textView;
        private final View itemView;
        private final ImageView imageView;

        public DeckListViewHolder(final View itemView) {
            super(itemView);
            this.itemView = itemView;
            textView = (TextView) itemView.findViewById(R.id.text_view);
            imageView = (ImageView) itemView.findViewById(R.id.image_view);
        }

        @Override
        public void bind(final AbstractDeck deck) {
            textView.setText(deck.getName());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((MainActivity) getActivity()).transactTo(DeckEditFragment.newInstance(deck.getId()), true);
                }
            });
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogUtils.showRenameDeckDialog(getContext(), deck);
                }
            });
        }
    }

    @NonNull
    @Override
    public String getTitle() {
        return getString(R.string.nav_deck_edit);
    }


}
