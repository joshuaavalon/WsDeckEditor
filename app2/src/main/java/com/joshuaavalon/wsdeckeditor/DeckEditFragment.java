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
import android.widget.TextView;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.joshuaavalon.wsdeckeditor.sdk.Card;
import com.joshuaavalon.wsdeckeditor.sdk.Deck;
import com.joshuaavalon.wsdeckeditor.sdk.data.CardRepository;
import com.joshuaavalon.wsdeckeditor.sdk.data.DeckRepository;
import com.joshuaavalon.wsdeckeditor.sdk.util.AbstractDeck;
import com.joshuaavalon.wsdeckeditor.sdk.util.DeckRecord;
import com.joshuaavalon.wsdeckeditor.view.BaseRecyclerViewHolder;
import com.joshuaavalon.wsdeckeditor.view.SelectableAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class DeckEditFragment extends BaseFragment implements
        SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor> {
    public static final int REQUEST_CARD_DETAIL = 1;
    private RecyclerView recyclerView;
    private CardRecyclerViewAdapter adapter;
    private static final String ARG_ID = "DeckEditFragment.arg.Id";
    private static final String ARG_SERIALS = "DeckEditFragment.arg.Serials";
    private LruCache<Card, Bitmap> bitmapCache;
    @Nullable
    private String title;
    private AbstractDeck abstractDeck;
    private List<DeckRecord> records;
    private Deck deck;

    @NonNull
    public static DeckEditFragment newInstance(final long id) {
        final DeckEditFragment fragment = new DeckEditFragment();
        final Bundle args = new Bundle();
        args.putLong(ARG_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bitmapCache = BitmapUtils.createBitmapCache();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_card_list, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        adapter = new CardRecyclerViewAdapter(new ArrayList<Multiset.Entry<Card>>());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        setHasOptionsMenu(true);
        getActivity().getSupportLoaderManager().restartLoader(LoaderId.DeckLoader, getArguments(), this);
        getActivity().getSupportLoaderManager().restartLoader(LoaderId.DeckRecordLoader, getArguments(), this);
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.action_add:
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
        if (deck == null) return false;
        final List<Multiset.Entry<Card>> entries = Lists.newArrayList(deck.getCardList().entrySet());
        final List<Multiset.Entry<Card>> filteredCardList = Lists.newArrayList(Iterables.filter(entries, new Predicate<Multiset.Entry<Card>>() {
            @Override
            public boolean apply(final Multiset.Entry<Card> input) {
                return input.getElement().getName().contains(query);
            }
        }));
        adapter.setModels(filteredCardList);
        recyclerView.scrollToPosition(0);
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LoaderId.DeckLoader) {
            abstractDeck = null;
            return DeckRepository.newDeckLoader(getContext(), args.getLong(ARG_ID));
        }
        if (id == LoaderId.DeckRecordLoader) {
            records = null;
            return DeckRepository.newDeckRecordLoader(getContext(), args.getLong(ARG_ID));
        }
        if (id == LoaderId.CardLoader) {
            final List<String> serials = args.getStringArrayList(ARG_SERIALS);
            if (serials != null)
                return CardRepository.newCardsLoader(getContext(), serials);
        }
        throw new IllegalArgumentException();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LoaderId.DeckLoader:
                abstractDeck = DeckRepository.toDeck(data);
                if (abstractDeck == null) return;
                title = abstractDeck.getName();
                getActivity().setTitle(getTitle());
                combineDeck();
                break;
            case LoaderId.DeckRecordLoader:
                records = DeckRepository.toDeckRecords(data);
                combineDeck();
                break;
            case LoaderId.CardLoader:
                deck = new Deck();
                deck.setId(abstractDeck.getId());
                deck.setName(abstractDeck.getName());
                final List<Card> cards = CardRepository.toCards(data);
                for (DeckRecord record : records) {
                    for (Card card : cards) {
                        if (!Objects.equals(card.getSerial(), record.getSerial())) continue;
                        deck.setCardCount(card, record.getCount());
                        break;
                    }
                }
                adapter.setModels(Lists.newArrayList(deck.getCardList().entrySet()));
                recyclerView.scrollToPosition(0);
                break;
        }
    }

    private void combineDeck() {
        if (abstractDeck == null || records == null) return;
        final Bundle args = new Bundle();
        args.putStringArrayList(ARG_SERIALS, Lists.newArrayList(Iterables.transform(records,
                new Function<DeckRecord, String>() {
                    @Nullable
                    @Override
                    public String apply(DeckRecord input) {
                        return input.getSerial();
                    }
                })));
        getActivity().getSupportLoaderManager().restartLoader(LoaderId.CardLoader, args, this);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //no-ops
    }

    private class CardRecyclerViewAdapter extends SelectableAdapter<Multiset.Entry<Card>, CardViewHolder> {
        public CardRecyclerViewAdapter(@NonNull final List<Multiset.Entry<Card>> items) {
            super(items);
        }

        @Override
        public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_deck_edit, parent, false);
            return new CardViewHolder(view);
        }
    }

    private class CardViewHolder extends BaseRecyclerViewHolder<Multiset.Entry<Card>> implements CardImageHolder {
        @NonNull
        private final ImageView imageView;
        @NonNull
        private final TextView nameTextView, serialTextView, countTextView;
        @NonNull
        private final View itemView, colorView;
        @NonNull
        private String imageName;


        public CardViewHolder(@NonNull final View itemView) {
            super(itemView);
            this.itemView = itemView;
            imageView = (ImageView) itemView.findViewById(R.id.card_image);
            nameTextView = (TextView) itemView.findViewById(R.id.card_name);
            serialTextView = (TextView) itemView.findViewById(R.id.card_serial);
            countTextView = (TextView) itemView.findViewById(R.id.card_count);
            colorView = itemView.findViewById(R.id.color_bar);
            imageName = "";
        }

        @Override
        public void bind(final Multiset.Entry<Card> entry) {
            final Card card = entry.getElement();
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final ArrayList<Card> entries = Lists.newArrayList(Iterables.transform(
                            deck.getCardList().entrySet(), new Function<Multiset.Entry<Card>, Card>() {
                                @Nullable
                                @Override
                                public Card apply(Multiset.Entry<Card> input) {
                                    return input.getElement();
                                }
                            }));
                    CardActivity.start(DeckEditFragment.this, REQUEST_CARD_DETAIL,
                            title, Lists.newArrayList(Iterables.transform(entries, new Function<Card, String>() {
                                @Nullable
                                @Override
                                public String apply(Card input) {
                                    return input.getSerial();
                                }
                            })),
                            entries.indexOf(card));
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
            countTextView.setText(String.valueOf(entry.getCount()));
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
                ((MainActivity) getActivity()).transactTo(CardListFragment.newInstance(filter), true);
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
