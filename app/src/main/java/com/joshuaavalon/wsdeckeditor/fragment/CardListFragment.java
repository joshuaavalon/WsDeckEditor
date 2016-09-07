package com.joshuaavalon.wsdeckeditor.fragment;


import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.activity.CardViewActivity;
import com.joshuaavalon.wsdeckeditor.model.Card;
import com.joshuaavalon.wsdeckeditor.model.Deck;
import com.joshuaavalon.wsdeckeditor.repository.CardRepository;
import com.joshuaavalon.wsdeckeditor.repository.DeckRepository;
import com.joshuaavalon.wsdeckeditor.repository.model.CardFilter;
import com.joshuaavalon.wsdeckeditor.repository.model.CardFilterItem;
import com.joshuaavalon.wsdeckeditor.view.ActionModeListener;
import com.joshuaavalon.wsdeckeditor.view.BaseRecyclerViewHolder;
import com.joshuaavalon.wsdeckeditor.view.ColorUtils;
import com.joshuaavalon.wsdeckeditor.view.SelectableAdapter;

import java.util.ArrayList;
import java.util.List;

public class CardListFragment extends BaseFragment implements SearchView.OnQueryTextListener {
    private static final String ARG_FILTER = "filter";

    private List<Card> resultCards;
    private CardRecyclerViewAdapter adapter;
    private ActionModeCallback actionModeCallback;
    @Nullable
    private ActionMode actionMode;
    private RecyclerView recyclerView;

    @NonNull
    public static CardListFragment newInstance(@NonNull final CardFilter filter) {
        final CardListFragment fragment = new CardListFragment();
        final Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_FILTER, filter.getParcelableList());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resultCards = new ArrayList<>();
        actionModeCallback = new ActionModeCallback();

        final Bundle args = getArguments();
        if (args == null) return;
        final List<CardFilterItem> filters = args.getParcelableArrayList(ARG_FILTER);
        if (filters != null)
            resultCards.addAll(CardRepository.getCards(new CardFilter(filters)));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_card_list, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        adapter = new CardRecyclerViewAdapter(resultCards, new ActionModeListener() {
            @Override
            public void onItemClicked(final int position) {
                toggleSelection(position);
            }

            @Override
            public boolean onItemLongClicked(final int position) {
                if (!startActionMode()) return false;
                toggleSelection(position);
                return true;
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        setHasOptionsMenu(true);
        return view;
    }

    private void toggleSelection(final int position) {
        if (actionMode == null) return;
        adapter.toggleSelection(position);
        final int count = adapter.getSelectedItemCount();
        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.invalidate();
        }
    }

    private boolean startActionMode() {
        final Activity parentActivity = getActivity();
        if (!(parentActivity instanceof AppCompatActivity) || actionMode != null) return false;
        actionMode = ((AppCompatActivity) parentActivity).startSupportActionMode(actionModeCallback);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.add_to_deck:
                startActionMode();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(final String query) {
        final List<Card> filteredCardList = Lists.newArrayList(Iterables.filter(resultCards, new Predicate<Card>() {
            @Override
            public boolean apply(final Card input) {
                return input.getName().contains(query) ||
                        input.getSerial().toUpperCase().contains(query.toUpperCase());
            }
        }));
        adapter.setModels(filteredCardList);
        recyclerView.scrollToPosition(0);
        return true;
    }

    private void showDeckSelectDialog(@NonNull final List<String> cardsToAdd) {
        final List<Deck> decks = DeckRepository.getDecks();
        new MaterialDialog.Builder(getContext())
                .iconRes(R.drawable.ic_assignment_black_24dp)
                .title(R.string.select_your_deck)
                .items(Lists.newArrayList(Iterables.transform(decks,
                        new Function<Deck, String>() {
                            @Override
                            public String apply(Deck input) {
                                return input.getName();
                            }
                        })))
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        final Deck currentDeck = decks.get(which);
                        for (String serial : cardsToAdd)
                            currentDeck.addIfNotExist(serial);
                        DeckRepository.save(currentDeck);
                        dialog.dismiss();
                    }
                })
                .positiveText(R.string.new_deck)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        showCreateDeckDialog(dialog, cardsToAdd);
                    }
                })
                .autoDismiss(false)
                .show();
    }

    private void showCreateDeckDialog(@NonNull final MaterialDialog parent,
                                      @NonNull final List<String> cardsToAdd) {
        new MaterialDialog.Builder(getContext())
                .iconRes(R.drawable.ic_add_black_24dp)
                .title(R.string.create_a_new_deck)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .positiveText(R.string.create_deck_create)
                .negativeText(R.string.cancel_button)
                .input(R.string.deck_name, 0, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        final Deck deck = new Deck();
                        deck.setName(input.toString());
                        for (String serial : cardsToAdd)
                            deck.addIfNotExist(serial);
                        DeckRepository.save(deck);
                        parent.dismiss();
                        showMessage(R.string.add_to_deck);
                    }
                })
                .show();
    }

    private class CardRecyclerViewAdapter extends SelectableAdapter<Card, CardViewHolder> {
        @Nullable
        private final ActionModeListener actionModeListener;

        public CardRecyclerViewAdapter(@NonNull final List<Card> items,
                                       @Nullable final ActionModeListener actionModeListener) {
            super(items);
            this.actionModeListener = actionModeListener;
        }

        @Override
        public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_card, parent, false);
            return new CardViewHolder(view, actionModeListener);
        }

        @Override
        public void onBindViewHolder(CardViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            holder.linearLayout.setActivated(isSelected(position));
        }
    }

    private class CardViewHolder extends BaseRecyclerViewHolder<Card>
            implements View.OnLongClickListener {
        @NonNull
        private final ImageView imageView;
        @NonNull
        private final TextView nameTextView;
        @NonNull
        private final TextView serialTextView;
        @NonNull
        private final View itemView;
        @NonNull
        private final LinearLayout linearLayout;
        @Nullable
        private final ActionModeListener actionModeListener;
        @NonNull
        private final View colorView;
        @NonNull
        private final TextView levelTextView;
        @NonNull
        private final TextView typeTextView;


        public CardViewHolder(@NonNull final View itemView, @Nullable final ActionModeListener actionModeListener) {
            super(itemView);
            this.itemView = itemView;
            imageView = (ImageView) itemView.findViewById(R.id.card_image);
            nameTextView = (TextView) itemView.findViewById(R.id.card_name);
            serialTextView = (TextView) itemView.findViewById(R.id.card_serial);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.card_background);
            colorView = itemView.findViewById(R.id.color_bar);
            levelTextView = (TextView) itemView.findViewById(R.id.card_level);
            typeTextView = (TextView) itemView.findViewById(R.id.card_type);
            itemView.setOnLongClickListener(this);
            this.actionModeListener = actionModeListener;
        }

        @Override
        public void bind(final Card card) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (actionMode == null) {
                        CardViewActivity.start(getActivity(), Lists.newArrayList(
                                Iterables.transform(resultCards, new Function<Card, String>() {
                                    @Override
                                    public String apply(Card input) {
                                        return input.getSerial();
                                    }
                                })), resultCards.indexOf(card));
                    } else {
                        if (actionModeListener == null) return;
                        actionModeListener.onItemClicked(getAdapterPosition());
                    }
                }
            });
            final Bitmap bitmap = CardRepository.getImage(card.getImage(), card.getType());
            imageView.setImageBitmap(bitmap);
            nameTextView.setText(card.getName());
            serialTextView.setText(card.getSerial());
            colorView.setBackgroundResource(card.getColor().getColorResId());
            linearLayout.setBackgroundResource(ColorUtils.getBackgroundDrawable(card.getColor()));
            if (card.getType() != Card.Type.Climax)
                levelTextView.setText(getString(R.string.card_level_prefix, String.valueOf(card.getLevel())));
            else
                levelTextView.setText(R.string.not_applicable);
            typeTextView.setText(card.getType().getResId());
        }

        @Override
        public boolean onLongClick(View view) {
            return actionModeListener != null &&
                    actionModeListener.onItemLongClicked(getAdapterPosition());
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(final ActionMode actionMode, final Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.menu_cardlist_actionmode, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(final ActionMode actionMode, final Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode actionMode, final MenuItem item) {
            switch (item.getItemId()) {
                case R.id.select_all:
                    adapter.selecAll();
                    return true;
                case R.id.add_to_deck:
                    final List<String> cardsToAdd = new ArrayList<>();
                    for (int index : adapter.getSelectedItems()) {
                        cardsToAdd.add(resultCards.get(index).getSerial());
                    }
                    showDeckSelectDialog(cardsToAdd);
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(final ActionMode actionMode) {
            adapter.clearSelection();
            CardListFragment.this.actionMode = null;
        }
    }
}
