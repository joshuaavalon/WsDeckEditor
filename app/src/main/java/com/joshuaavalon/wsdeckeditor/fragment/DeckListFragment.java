package com.joshuaavalon.wsdeckeditor.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.activity.Transactable;
import com.joshuaavalon.wsdeckeditor.model.Deck;
import com.joshuaavalon.wsdeckeditor.repository.DeckRepository;
import com.joshuaavalon.wsdeckeditor.view.AnimatedRecyclerAdapter;
import com.joshuaavalon.wsdeckeditor.view.BaseRecyclerViewHolder;

import java.util.ArrayList;
import java.util.List;

public class DeckListFragment extends BaseFragment implements SearchView.OnQueryTextListener {
    private RecyclerView recyclerView;
    private DeckListAdapter adapter;
    private List<Deck> decks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_deck_list, container, false);
        decks = DeckRepository.getDecks();

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
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

    private void showRenameDialog(@NonNull final Deck deck) {
        new MaterialDialog.Builder(getContext())
                .title(R.string.rename_deck)
                .content(deck.getName())
                .inputType(InputType.TYPE_CLASS_TEXT)
                .positiveText(R.string.rename_button)
                .negativeText(R.string.cancel_button)
                .input(getString(R.string.deck_name), deck.getName(), false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        final Deck renameDeck = DeckRepository.getDeckById(deck.getId()).get();
                        renameDeck.setName(input.toString());
                        DeckRepository.save(renameDeck);
                        refresh();
                    }
                })
                .show();
    }

    private void showCreateDeckDialog() {
        new MaterialDialog.Builder(getContext())
                .title(R.string.create_a_new_deck)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .positiveText(R.string.create_deck_create)
                .negativeText(R.string.cancel_button)
                .input(R.string.deck_name, 0, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        final Deck deck = new Deck();
                        deck.setName(input.toString());
                        DeckRepository.save(deck);
                        refresh();
                    }
                })
                .show();
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

        public DeckListViewHolder(final View itemView) {
            super(itemView);
            this.itemView = itemView;
            textView = (TextView) itemView.findViewById(R.id.text_view);
        }

        @Override
        public void bind(final Deck deck) {
            textView.setText(
                    deck.getName());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!(getActivity() instanceof Transactable)) return;
                    final Transactable transactable = (Transactable) getActivity();
                    transactable.transactTo(DeckEditFragment.newInstance(deck.getId()));
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    showRenameDialog(deck);
                    return true;
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
                showCreateDeckDialog();
            }
        });
        fab.setImageResource(R.drawable.ic_add_white_24dp);
    }

    @Override
    public void onPause() {
        super.onPause();
        final FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        if (fab != null)
            fab.hide();
    }
}
