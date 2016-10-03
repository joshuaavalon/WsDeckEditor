package com.joshuaavalon.wsdeckeditor;


import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.util.LruCache;
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
import com.joshuaavalon.wsdeckeditor.sdk.Card;
import com.joshuaavalon.wsdeckeditor.sdk.data.CardRepository;
import com.joshuaavalon.wsdeckeditor.sdk.data.DeckRepository;
import com.joshuaavalon.wsdeckeditor.sdk.util.AbstractDeck;
import com.joshuaavalon.wsdeckeditor.view.ActionModeListener;
import com.joshuaavalon.wsdeckeditor.view.BaseRecyclerViewHolder;
import com.joshuaavalon.wsdeckeditor.view.ColorUtils;
import com.joshuaavalon.wsdeckeditor.view.SelectableAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class CardListFragment extends BaseFragment implements ActionMode.Callback, ActionModeListener,
        SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor>, OnBackPressedListener {
    public static final int REQUEST_CARD_DETAIL = 1;
    private RecyclerView recyclerView;
    private CardRecyclerViewAdapter adapter;
    private ArrayList<Card> resultCards;
    @Nullable
    private ActionMode actionMode;
    private static final String ARG_EXPANSION = "CardListFragment.arg.Expansion";
    private static final String ARG_FILTER = "CardListFragment.arg.Filter";
    private static final int STACK_SIZE = 5;
    private LruCache<Card, Bitmap> bitmapCache;
    @Nullable
    private String title;
    private Stack<Bundle> argStack;
    private Stack<String> titleStack;

    @NonNull
    public static CardListFragment newInstance(@NonNull final String expansion) {
        final CardListFragment fragment = new CardListFragment();
        final Bundle args = new Bundle();
        args.putString(ARG_EXPANSION, expansion);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    public static CardListFragment newInstance(@NonNull final CardRepository.Filter filter) {
        final CardListFragment fragment = new CardListFragment();
        final Bundle args = new Bundle();
        args.putParcelable(ARG_FILTER, filter);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bitmapCache = BitmapUtils.createBitmapCache();
        argStack = new SizedStack<>(STACK_SIZE);
        titleStack = new SizedStack<>(STACK_SIZE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_card_list, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        resultCards = new ArrayList<>();
        adapter = new CardRecyclerViewAdapter(new ArrayList<>(resultCards), this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        setHasOptionsMenu(true);
        argStack.add(getArguments());
        getActivity().getSupportLoaderManager().restartLoader(LoaderId.CardListLoader, getArguments(), this);
        initTitle();
        return view;
    }

    private void initTitle() {
        final Bundle args = getArguments();
        if (args.containsKey(ARG_EXPANSION))
            title = args.getString(ARG_EXPANSION);
        else if (args.containsKey(ARG_FILTER))
            title = getString(R.string.search_result);
        titleStack.add(getTitle());
    }

    private void toggleSelection(final int position) {
        if (actionMode == null) return;
        adapter.toggleSelection(position);
        final int count = adapter.getSelectedItemCount();
        if (count == 0) { //TODO: PreferenceRepository.getAutoClose()
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
                startActionMode();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
                final List<String> cardsToAdd = new ArrayList<>();
                for (int index : adapter.getSelectedItems()) {
                    cardsToAdd.add(resultCards.get(index).getSerial());
                }
                //TODO
/*                if (cardsToAdd.size() > WsApplication.CARD_TYPE_LIMIT)
                    showMessage(R.string.msg_select_too_many);
                else
                    showDeckSelectDialog(cardsToAdd);*/
                getActivity().getSupportLoaderManager().restartLoader(LoaderId.DeckListLoader, getArguments(), this);
                return true;
            default:
                return false;
        }
    }


    @Override
    public void onDestroyActionMode(final ActionMode actionMode) {
        adapter.clearSelection();
        this.actionMode = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LoaderId.CardListLoader:
                final String expansion = args.getString(ARG_EXPANSION);
                if (expansion != null)
                    return CardRepository.newCardsLoader(getContext(), expansion, PreferenceRepository.getShowLimit(getContext()), 0);
                final CardRepository.Filter filter = args.getParcelable(ARG_FILTER);
                if (filter != null)
                    return CardRepository.newCardsLoader(getContext(), filter, PreferenceRepository.getShowLimit(getContext()), 0);
            case LoaderId.DeckListLoader:
                return DeckRepository.newDecksLoader(getContext());
        }
        throw new IllegalArgumentException();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LoaderId.CardListLoader:
                resultCards.clear();
                resultCards.addAll(CardRepository.toCards(data));
                adapter.setModels(new ArrayList<>(resultCards));
                recyclerView.scrollToPosition(0);
                getActivity().setTitle(getTitle());
                break;
            case LoaderId.DeckListLoader:
                final List<AbstractDeck> decks = DeckRepository.toDecks(data);
                DialogUtils.showDeckSelectDialog(getContext(),
                        Lists.newArrayList(Iterables.transform(decks, new Function<AbstractDeck, String>() {
                            @Nullable
                            @Override
                            public String apply(AbstractDeck input) {
                                return input.getName();
                            }
                        })),
                        new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                                for (int index : adapter.getSelectedItems()) {
                                    DeckRepository.updateDeckCount(getContext(), decks.get(position).getId(), resultCards.get(index).getSerial(), 1);
                                }
                            }
                        });
                break;
        }
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
        final Bundle args = argStack.peek();
        getActivity().getSupportLoaderManager().restartLoader(LoaderId.CardListLoader, args, this);
        title = titleStack.peek();
        return true;
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
        private String imageName;


        public CardViewHolder(@NonNull final View itemView, @Nullable final ActionModeListener actionModeListener) {
            super(itemView);
            this.itemView = itemView;
            imageView = (ImageView) itemView.findViewById(R.id.card_image);
            nameTextView = (TextView) itemView.findViewById(R.id.card_name);
            serialTextView = (TextView) itemView.findViewById(R.id.card_serial);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.card_background);
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
                        CardActivity.start(CardListFragment.this, REQUEST_CARD_DETAIL,
                                title, Lists.newArrayList(Iterables.transform(resultCards, new Function<Card, String>() {
                                    @Nullable
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
            imageName = card.getImageName();
            final Bitmap squareBitmap = bitmapCache.get(card);
            if (squareBitmap != null) {
                imageView.setImageDrawable(BitmapUtils.toRoundDrawable(getResources(), squareBitmap));
            } else {
                imageView.setImageDrawable(null);
                new LoadCircularCardImageTask(getContext(), bitmapCache, this, card).execute();
            }
            nameTextView.setText(card.getName());
            serialTextView.setText(card.getSerial());
            colorView.setBackgroundResource(card.getColor().getColorId());
            linearLayout.setBackgroundResource(ColorUtils.getBackgroundDrawable(card.getColor()));
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
    public void onDestroyView() {
        super.onDestroyView();
        bitmapCache.evictAll();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CARD_DETAIL) return;
        if (resultCode == RESULT_CANCELED) {
            final int position = data.getIntExtra(CardActivity.RESULT_POSITION, -1);
            if (position != -1)
                recyclerView.scrollToPosition(position);
        } else if (resultCode == RESULT_OK) {
            final CardRepository.Filter filter = data.getParcelableExtra(CardActivity.RESULT_FILTER);
            if (filter != null) {
                final Bundle args = new Bundle();
                args.putParcelable(ARG_FILTER, filter);
                argStack.add(args);
                final String[] keywords = new String[filter.getKeyword().size()];
                filter.getKeyword().toArray(keywords);
                title = keywords[0];
                titleStack.add(title);
                getActivity().getSupportLoaderManager().restartLoader(LoaderId.CardListLoader, args, this);
            }
        }
    }

    @NonNull
    @Override
    public String getTitle() {
        if (title != null)
            return title;
        return super.getTitle();
    }
}
