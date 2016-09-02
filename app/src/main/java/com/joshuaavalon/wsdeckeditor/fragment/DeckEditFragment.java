package com.joshuaavalon.wsdeckeditor.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.joshuaavalon.wsdeckeditor.Handler;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.activity.CardViewActivity;
import com.joshuaavalon.wsdeckeditor.activity.MainActivity;
import com.joshuaavalon.wsdeckeditor.fragment.dialog.ChangeCardCountDialogFragment;
import com.joshuaavalon.wsdeckeditor.fragment.dialog.DeckRenameDialogFragment;
import com.joshuaavalon.wsdeckeditor.fragment.dialog.SortCardDialogFragment;
import com.joshuaavalon.wsdeckeditor.model.Card;
import com.joshuaavalon.wsdeckeditor.model.Deck;
import com.joshuaavalon.wsdeckeditor.repository.CardRepository;
import com.joshuaavalon.wsdeckeditor.repository.DeckRepository;
import com.joshuaavalon.wsdeckeditor.repository.PreferenceRepository;
import com.joshuaavalon.wsdeckeditor.view.BaseRecyclerViewHolder;
import com.joshuaavalon.wsdeckeditor.view.ColorUtils;
import com.joshuaavalon.wsdeckeditor.view.SelectableAdapter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DeckEditFragment extends BaseFragment implements Handler<Object>, ChangeCardCountDialogFragment.Callback, DeckRenameDialogFragment.Callback {
    private static final String ARG_DECK_ID = "deckId";
    private CardRecyclerViewAdapter adapter;
    private Deck deck;

    public static DeckEditFragment newInstance(final long deckId) {
        final DeckEditFragment fragment = new DeckEditFragment();
        final Bundle args = new Bundle();
        args.putLong(ARG_DECK_ID, deckId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        if (args == null) return;
        final Optional<Deck> deckOptional = DeckRepository.getDeckById(args.getLong(ARG_DECK_ID));
        if (deckOptional.isPresent())
            deck = deckOptional.get();
        else
            deck = new Deck();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_sort:
                SortCardDialogFragment.start(getFragmentManager(), DeckEditFragment.this);
                return true;
            case R.id.menu_delete:
                DeckRepository.delete(deck);
                getFragmentManager().popBackStack();
                return true;
            case R.id.menu_rename:
                DeckRenameDialogFragment.start(getFragmentManager(),
                        DeckEditFragment.this,
                        deck);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_deckedit, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_deck_edit, container, false);
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        adapter = new CardRecyclerViewAdapter(deck.getList());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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
                        if (!(viewHolder instanceof CardViewHolder)) return;
                        deck.setCount(adapter.getCards().get(viewHolder.getAdapterPosition()), 0);
                        refresh();
                    }
                });
        itemTouchHelper.attachToRecyclerView(recyclerView);
        setHasOptionsMenu(true);
        return view;
    }

    private void refresh() {
        final Activity activity = getActivity();
        if (activity instanceof MainActivity)
            ((MainActivity) activity).setTitle(deck.getName());
        sort(PreferenceRepository.getSortOrder());
        if (PreferenceRepository.getAutoSave())
            DeckRepository.save(deck);
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
        final FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        if (fab == null) return;
        fab.show();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeckRepository.save(deck);
                showMessage(R.string.deck_saved);
            }
        });
        fab.setImageResource(R.drawable.ic_save_white_24dp);
    }


    @Override
    public void onPause() {
        super.onPause();
        final FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        if (fab != null)
            fab.hide();

        final Activity activity = getActivity();
        if (activity instanceof MainActivity)
            ((MainActivity) activity).removeTitle();
    }

    @Override
    public void handle(Object result) {
        if (result instanceof Card.SortOrder) {
            final Card.SortOrder order = (Card.SortOrder) result;
            PreferenceRepository.setSortOrder(order);
            sort(order);
        }
    }

    private void sort(Card.SortOrder order) {
        final List<Multiset.Entry<Card>> lists = Lists.newArrayList(deck.getList().entrySet());
        Collections.sort(lists, transformComparator(Card.Comparator(order)));
        adapter.setModels(lists);
    }

    @Override
    public void changeCardCount(@NonNull String serial, int count) {
        deck.setCount(serial, count);
        refresh();
    }

    @Override
    public void changeDeckName(long deckId, @NonNull String title) {
        deck.setName(title);
        refresh();
    }

    private class CardRecyclerViewAdapter extends SelectableAdapter<Multiset.Entry<Card>, CardViewHolder> {
        public CardRecyclerViewAdapter(@NonNull final Multiset<Card> items) {
            super(Lists.newArrayList(items.entrySet()));
        }

        @Override
        public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_card, parent, false);
            return new CardViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CardViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            holder.linearLayout.setActivated(isSelected(position));
        }

        @NonNull
        public List<Card> getCards() {
            return Lists.newArrayList(
                    Iterables.transform(models, new Function<Multiset.Entry<Card>, Card>() {
                        @Override
                        public Card apply(Multiset.Entry<Card> input) {
                            return input.getElement();
                        }
                    }));
        }
    }

    private static Comparator<Multiset.Entry<Card>> transformComparator(
            @NonNull final Comparator<Card> comparator) {
        return new Comparator<Multiset.Entry<Card>>() {
            @Override
            public int compare(Multiset.Entry<Card> left, Multiset.Entry<Card> right1) {
                return comparator.compare(left.getElement(), right1.getElement());
            }
        };
    }

    private class CardViewHolder extends BaseRecyclerViewHolder<Multiset.Entry<Card>> {
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
        @NonNull
        private final View colorView;
        @NonNull
        private final TextView countTextView;
        @NonNull
        private final TextView typeTextView;

        public CardViewHolder(@NonNull final View itemView) {
            super(itemView);
            this.itemView = itemView;
            imageView = (ImageView) itemView.findViewById(R.id.card_image);
            nameTextView = (TextView) itemView.findViewById(R.id.card_name);
            serialTextView = (TextView) itemView.findViewById(R.id.card_serial);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.card_background);
            colorView = itemView.findViewById(R.id.color_bar);
            countTextView = (TextView) itemView.findViewById(R.id.card_level);
            typeTextView = (TextView) itemView.findViewById(R.id.card_type);
        }

        @Override
        public void bind(final Multiset.Entry<Card> entry) {
            final Card card = entry.getElement();
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final List<Card> cards = Lists.newArrayList(deck.getList().elementSet());
                    CardViewActivity.start(getContext(), Lists.newArrayList(
                            Iterables.transform(cards, new Function<Card, String>() {
                                @Override
                                public String apply(Card input) {
                                    return input.getSerial();
                                }
                            })), cards.indexOf(card));
                }
            });
            final Bitmap bitmap = CardRepository.getImage(card.getImage(), card.getType());
            imageView.setImageBitmap(bitmap);
            nameTextView.setText(card.getName());
            serialTextView.setText(card.getSerial());
            colorView.setBackgroundResource(ColorUtils.getColor(card.getColor()));
            linearLayout.setBackgroundResource(ColorUtils.getBackgroundDrawable(card.getColor()));
            countTextView.setText(String.valueOf(entry.getCount()));
            countTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ChangeCardCountDialogFragment.start(getFragmentManager(),
                            DeckEditFragment.this,
                            card.getSerial());
                }
            });
            typeTextView.setText(card.getType().getResId());
        }
    }
}

