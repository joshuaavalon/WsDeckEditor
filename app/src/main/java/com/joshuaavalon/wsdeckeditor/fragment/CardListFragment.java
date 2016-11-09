package com.joshuaavalon.wsdeckeditor.fragment;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.joshuaavalon.wsdeckeditor.CardImageHolder;
import com.joshuaavalon.wsdeckeditor.DeckUtils;
import com.joshuaavalon.wsdeckeditor.DialogUtils;
import com.joshuaavalon.wsdeckeditor.LoaderId;
import com.joshuaavalon.wsdeckeditor.MainActivity;
import com.joshuaavalon.wsdeckeditor.OnBackPressedListener;
import com.joshuaavalon.wsdeckeditor.PreferenceRepository;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.SizedStack;
import com.joshuaavalon.wsdeckeditor.SnackBarSupport;
import com.joshuaavalon.wsdeckeditor.sdk.Card;
import com.joshuaavalon.wsdeckeditor.sdk.Deck;
import com.joshuaavalon.wsdeckeditor.sdk.data.CardRepository;
import com.joshuaavalon.wsdeckeditor.sdk.data.DeckRepository;
import com.joshuaavalon.wsdeckeditor.sdk.task.CardListLoadTask;
import com.joshuaavalon.wsdeckeditor.sdk.task.ResultTask;
import com.joshuaavalon.wsdeckeditor.sdk.util.AbstractDeck;
import com.joshuaavalon.wsdeckeditor.view.ActionModeListener;
import com.joshuaavalon.wsdeckeditor.view.BaseRecyclerViewHolder;
import com.joshuaavalon.wsdeckeditor.view.ColorUtils;
import com.joshuaavalon.wsdeckeditor.view.SelectableAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static android.app.Activity.RESULT_OK;

public class CardListFragment extends ImageListFragment implements ActionMode.Callback, ActionModeListener,
        SearchView.OnQueryTextListener, OnBackPressedListener, ResultTask.CallBack<List<Card>>, LoaderManager.LoaderCallbacks<Cursor> {
    public static final int REQUEST_CARD_DETAIL = 1;
    private static final String ARG_FILTER = "CardListFragment.arg.Filter";
    private static final String ARG_TITLE = "CardListFragment.arg.Title";
    private static final int STACK_SIZE = 5;
    private static final int SELECT_CARD_LIMIT = 70;
    private RecyclerView recyclerView;
    private CardRecyclerViewAdapter adapter;
    private ArrayList<Card> resultCards;
    @Nullable
    private ActionMode actionMode;
    @Nullable
    private String title;
    private Stack<CardRepository.Filter> argStack;
    private Stack<String> titleStack;
    private List<AbstractDeck> decks;
    private int selectedPosition = -1;

    @NonNull
    public static CardListFragment newInstance(@NonNull final Context context, @NonNull final String expansion) {
        final CardRepository.Filter filter = new CardRepository.Filter();
        filter.setExpansion(expansion);
        filter.setNormalOnly(PreferenceRepository.getHideNormal(context));
        return newInstance(expansion, filter);
    }

    @NonNull
    public static CardListFragment newInstance(@NonNull final CardRepository.Filter filter) {
        final CardListFragment fragment = new CardListFragment();
        final Bundle args = new Bundle();
        args.putParcelable(ARG_FILTER, filter);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    public static CardListFragment newInstance(@NonNull final String title,
                                               @NonNull final CardRepository.Filter filter) {
        final CardListFragment fragment = new CardListFragment();
        final Bundle args = new Bundle();
        args.putParcelable(ARG_FILTER, filter);
        args.putString(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        argStack = new SizedStack<>(STACK_SIZE);
        titleStack = new SizedStack<>(STACK_SIZE);
        final CardRepository.Filter filter = getArguments().getParcelable(ARG_FILTER);
        if (filter != null)
            argStack.add(filter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_card_list, container, false);
        decks = new ArrayList<>();
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        resultCards = new ArrayList<>();
        adapter = new CardRecyclerViewAdapter(new ArrayList<>(resultCards), this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        setHasOptionsMenu(true);
        final CardRepository.Filter filter = getArguments().getParcelable(ARG_FILTER);
        if (filter != null)
            reload(filter);
        initTitle();
        getActivity().getSupportLoaderManager().initLoader(LoaderId.DeckListLoader, getArguments(), this);
        return view;
    }

    private void initTitle() {
        final Bundle args = getArguments();
        if (args.containsKey(ARG_TITLE))
            title = args.getString(ARG_TITLE);
        else
            title = getString(R.string.search_result);
        titleStack.add(getTitle());
    }

    private void toggleSelection(final int position) {
        if (actionMode == null) return;
        adapter.toggleSelection(position);
        final int count = adapter.getSelectedItemCount();
        if (count == 0 && PreferenceRepository.getAutoClose(getContext())) {
            actionMode.finish();
        } else {
            actionMode.invalidate();
        }
    }

    private boolean startActionMode() {
        actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.action_add:
                selectDeckDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void selectDeckDialog() {
        if (decks.size() > 0)
            DialogUtils.showDeckSelectDialog(getContext(), getDeckName(),
                    selectedPosition,
                    new MaterialDialog.ListCallbackSingleChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                            selectedPosition = which;
                            PreferenceRepository.setSelectedDeck(getContext(), decks.get(which).getId());
                            return true;
                        }
                    });
        else
            showNoDeckFound();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_card_list, menu);
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

    @Override
    public boolean onCreateActionMode(final ActionMode actionMode, final Menu menu) {
        actionMode.getMenuInflater().inflate(R.menu.action_mode_card_list, menu);
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
                adapter.selectAll();
                return true;
            case R.id.action_add:
                addSelectedCard();
                return true;
            default:
                return false;
        }
    }

    private void addSelectedCard() {
        if (adapter.getSelectedItemCount() <= SELECT_CARD_LIMIT) {
            if (decks.size() > 0)
                DialogUtils.showDeckSelectDialog(getContext(), getDeckName(),
                        new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                final long id = decks.get(which).getId();
                                if (!DeckUtils.checkDeckCards(getContext(), id)) {
                                    showMessage(R.string.msg_cards_deck);
                                    return;
                                }
                                for (int index : adapter.getSelectedItems())
                                    if (PreferenceRepository.getAddIfNotExist(getContext()))
                                        DeckRepository.addCardIfNotExist(getContext(), id, resultCards.get(index).getSerial());
                                    else
                                        DeckRepository.addCard(getContext(), id, resultCards.get(index).getSerial());
                                showMessage(R.string.msg_add_to_deck);
                                if (actionMode != null && PreferenceRepository.getAutoClose(getContext()))
                                    actionMode.finish();
                            }
                        });
            else
                showNoDeckFound();
        } else
            showMessage(R.string.msg_select_too_many);
    }

    private List<String> getDeckName() {
        return Lists.newArrayList(Iterables.transform(decks, new Function<AbstractDeck, String>() {
            @Nullable
            @Override
            public String apply(AbstractDeck input) {
                return input.getName();
            }
        }));
    }

    @Override
    public void onDestroyActionMode(final ActionMode actionMode) {
        adapter.clearSelection();
        this.actionMode = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LoaderId.DeckListLoader:
                return DeckRepository.newDecksLoader(getContext());
        }
        throw new IllegalArgumentException();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LoaderId.DeckListLoader:
                decks = DeckRepository.toDecks(data);
                updateSelectedPosition();
                break;
        }
    }

    private void updateSelectedPosition() {
        final long id = PreferenceRepository.getSelectedDeck(getContext());
        if (id < 0) return;
        selectedPosition = Iterables.indexOf(decks, new Predicate<AbstractDeck>() {
            @Override
            public boolean apply(@Nullable AbstractDeck input) {
                return input != null && input.getId() == id;
            }
        });
    }

    private void showNoDeckFound() {
        Snackbar.make(((SnackBarSupport) getActivity()).getCoordinatorLayout(), R.string.msg_no_deck, Snackbar.LENGTH_LONG)
                .setAction(R.string.dialog_create_button, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogUtils.showCreateDeckDialog(getContext(), new DialogUtils.CreateDeckCallback() {
                            @Override
                            public void onCreate(@NonNull Deck deck) {
                                PreferenceRepository.setSelectedDeck(getContext(), deck.getId());
                                updateSelectedPosition();
                            }
                        });
                    }
                })
                .show();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //no-ops
    }

    @Override
    public boolean onBackPressed() {
        if (argStack.size() <= 1) return false;
        argStack.pop();
        titleStack.pop();
        reload(argStack.peek());
        title = titleStack.peek();
        return true;
    }

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CARD_DETAIL) return;
        if (resultCode != RESULT_OK) return;
        final CardRepository.Filter filter = data.getParcelableExtra(CardDetailFragment.RESULT_FILTER);
        if (filter == null) return;
        filter.setNormalOnly(PreferenceRepository.getHideNormal(getContext()));
        argStack.add(filter);
        final String[] keywords = new String[filter.getKeyword().size()];
        filter.getKeyword().toArray(keywords);
        title = keywords[0];
        titleStack.add(title);
        reload(filter);
    }

    private void reload(@NonNull final CardRepository.Filter filter) {
        new CardListLoadTask(this, filter, PreferenceRepository.getShowLimit(getContext()), 0).execute(getContext());
    }

    private void addCardToDeck(@NonNull final Card card) {
        if (decks.size() == 0) {
            showNoDeckFound();
            return;
        }
        if (selectedPosition < 0 || selectedPosition >= decks.size()) {
            showMessage(R.string.msg_no_select_deck);
            return;
        }
        final long id = decks.get(selectedPosition).getId();
        if (!DeckUtils.checkDeckCards(getContext(), id)) {
            showMessage(R.string.msg_cards_deck);
            return;
        }

        final int count = PreferenceRepository.getAddIfNotExist(getContext()) ?
                DeckRepository.addCardIfNotExist(getContext(), id, card.getSerial()) :
                DeckRepository.addCard(getContext(), id, card.getSerial());
        if (count > 0)
            showMessage(getString(R.string.msg_add_to_deck_single, count));
        else
            showMessage(R.string.msg_deck_error);
    }

    @NonNull
    @Override
    public String getTitle() {
        if (title != null)
            return title;
        return super.getTitle();
    }

    @Override
    public void onResult(List<Card> result) {
        resultCards.clear();
        resultCards.addAll(result);
        adapter.setModels(new ArrayList<>(resultCards));
        recyclerView.scrollToPosition(0);
        getActivity().setTitle(getTitle());
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
            implements View.OnLongClickListener, CardImageHolder {
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
        private final ImageView actionView;
        @NonNull
        private String imageName;


        public CardViewHolder(@NonNull final View itemView, @Nullable final ActionModeListener actionModeListener) {
            super(itemView);
            this.itemView = itemView;
            imageView = (ImageView) itemView.findViewById(R.id.card_image);
            nameTextView = (TextView) itemView.findViewById(R.id.card_name);
            serialTextView = (TextView) itemView.findViewById(R.id.card_serial);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.card_background);
            actionView = (ImageView) itemView.findViewById(R.id.image_view);
            colorView = itemView.findViewById(R.id.color_bar);
            itemView.setOnLongClickListener(this);
            this.actionModeListener = actionModeListener;
            imageName = "";
        }

        @Override
        public void bind(final Card card) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (actionMode == null) {
                        final Fragment fragment = CardDetailFragment.newInstance(card);
                        fragment.setTargetFragment(CardListFragment.this, REQUEST_CARD_DETAIL);
                        ((MainActivity) getActivity()).transactTo(fragment, true);
                    } else {
                        if (actionModeListener == null) return;
                        actionModeListener.onItemClicked(getAdapterPosition());
                    }
                }
            });
            imageName = card.getImage();
            loadImage(card, this);
            nameTextView.setText(card.getName());
            serialTextView.setText(getString(R.string.format_card_detail, card.getSerial(),
                    card.getLevel(), getString(card.getType().getStringId())));
            colorView.setBackgroundResource(card.getColor().getColorId());
            linearLayout.setBackgroundResource(ColorUtils.getBackgroundDrawable(card.getColor()));
            actionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addCardToDeck(card);
                }
            });
        }

        @Override
        public boolean onLongClick(View view) {
            return actionModeListener != null &&
                    actionModeListener.onItemLongClicked(getAdapterPosition());
        }

        @NonNull
        @Override
        public ImageView getImageView() {
            return imageView;
        }

        @NonNull
        @Override
        public String getImageName() {
            return imageName;
        }
    }
}
