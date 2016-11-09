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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.base.Function;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.joshuaavalon.wsdeckeditor.CardImageHolder;
import com.joshuaavalon.wsdeckeditor.CardOrder;
import com.joshuaavalon.wsdeckeditor.DialogUtils;
import com.joshuaavalon.wsdeckeditor.LoaderId;
import com.joshuaavalon.wsdeckeditor.MainActivity;
import com.joshuaavalon.wsdeckeditor.PreferenceRepository;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.SnackBarSupport;
import com.joshuaavalon.wsdeckeditor.sdk.Card;
import com.joshuaavalon.wsdeckeditor.sdk.Deck;
import com.joshuaavalon.wsdeckeditor.sdk.data.CardRepository;
import com.joshuaavalon.wsdeckeditor.sdk.data.DeckRepository;
import com.joshuaavalon.wsdeckeditor.sdk.task.DeckListLoadTask;
import com.joshuaavalon.wsdeckeditor.sdk.task.ResultTask;
import com.joshuaavalon.wsdeckeditor.sdk.util.AbstractDeck;
import com.joshuaavalon.wsdeckeditor.sdk.util.DeckRecord;
import com.joshuaavalon.wsdeckeditor.view.BaseRecyclerViewHolder;
import com.joshuaavalon.wsdeckeditor.view.SelectableAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class DeckEditFragment extends ImageListFragment implements LoaderManager.LoaderCallbacks<Cursor>,
        ResultTask.CallBack<List<Card>> {
    public static final int REQUEST_CARD_DETAIL = 1;
    private static final String ARG_ID = "DeckEditFragment.arg.Id";
    private RecyclerView recyclerView;
    private CardRecyclerViewAdapter adapter;
    @Nullable
    private String title;
    private AbstractDeck abstractDeck;
    private List<DeckRecord> records;
    private Deck deck;
    private Comparator<Multiset.Entry<Card>> comparator;

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
        comparator = new SerialComparator();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_deck_edit, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        adapter = new CardRecyclerViewAdapter(new ArrayList<Multiset.Entry<Card>>());
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
                        final Multiset.Entry<Card> entry = adapter.getModels().get(viewHolder.getAdapterPosition());
                        DeckRepository.updateDeckCount(getContext(), deck.getId(), entry.getElement().getSerial(), 0);
                        Snackbar.make(((SnackBarSupport) getActivity()).getCoordinatorLayout(), R.string.msg_remove_card, Snackbar.LENGTH_LONG)
                                .setAction(R.string.dialog_undo_button, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        DeckRepository.updateDeckCount(getContext(), deck.getId(), entry.getElement().getSerial(), entry.getCount());
                                    }
                                })
                                .show();
                    }
                });
        itemTouchHelper.attachToRecyclerView(recyclerView);
        setHasOptionsMenu(true);
        getActivity().getSupportLoaderManager().restartLoader(LoaderId.DeckLoader, getArguments(), this);
        getActivity().getSupportLoaderManager().restartLoader(LoaderId.DeckRecordLoader, getArguments(), this);
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.action_info:
                if (deck != null)
                    DialogUtils.showDeckInfoDialog(getContext(), deck);
                return true;
            case R.id.action_sort:
                showSortDialog();
                return true;
            case R.id.action_delete:
                if (deck == null) return true;
                new MaterialDialog.Builder(getContext())
                        .title(R.string.dialog_delete_deck)
                        .positiveText(R.string.dialog_delete_button)
                        .negativeText(R.string.dialog_cancel_button)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                final Context context = getActivity().getApplicationContext();
                                getActivity().getSupportFragmentManager().popBackStack();
                                DeckRepository.deleteDeck(context, deck);
                            }
                        })
                        .show();
                return true;
            case R.id.action_copy:
                if (deck != null) {
                    DeckRepository.createDeck(getContext(), deck);
                    showMessage(R.string.msg_deck_duplicated);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_deck_edit, menu);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LoaderId.DeckLoader:
                abstractDeck = null;
                return DeckRepository.newDeckLoader(getContext(), args.getLong(ARG_ID));
            case LoaderId.DeckRecordLoader:
                records = null;
                return DeckRepository.newDeckRecordLoader(getContext(), args.getLong(ARG_ID));
            default:
                throw new IllegalArgumentException();
        }
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
        }
    }

    private void resetDeck() {
        final List<Multiset.Entry<Card>> cardEntries = Lists.newArrayList(deck.getCardList().entrySet());
        Collections.sort(cardEntries, comparator);
        adapter.setModels(cardEntries);
        recyclerView.scrollToPosition(0);
    }

    private void changeComparator(@NonNull final Comparator<Multiset.Entry<Card>> comparator) {
        this.comparator = comparator;
        resetDeck();
    }

    private void combineDeck() {
        if (abstractDeck == null || records == null) return;
        final List<String> serials = Lists.newArrayList(Iterables.transform(records,
                new Function<DeckRecord, String>() {
                    @Nullable
                    @Override
                    public String apply(DeckRecord input) {
                        return input.getSerial();
                    }
                }));
        new DeckListLoadTask(this, serials).execute(getContext());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //no-ops
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CARD_DETAIL) return;
        if (resultCode != RESULT_OK) return;
        final CardRepository.Filter filter = data.getParcelableExtra(CardDetailFragment.RESULT_FILTER);
        final String title = data.getStringExtra(CardDetailFragment.RESULT_TITLE);
        ((MainActivity) getActivity()).transactTo(CardListFragment.newInstance(title, filter), true);
    }

    @NonNull
    @Override
    public String getTitle() {
        if (title != null)
            return title;
        return super.getTitle();
    }

    private void showSortDialog() {
        new MaterialDialog.Builder(getContext())
                .title(R.string.dialog_sort_by)
                .items(R.array.sort_type)
                .itemsCallbackSingleChoice(PreferenceRepository.getSortOrder(getContext()).ordinal(),
                        new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog,
                                                       View itemView,
                                                       int which,
                                                       CharSequence text) {
                                final CardOrder order = CardOrder.values()[which];
                                PreferenceRepository.setSortOrder(getContext(), order);
                                switch (order) {
                                    case Serial:
                                        changeComparator(new SerialComparator());
                                        break;
                                    case Level:
                                        changeComparator(new LevelComparator());
                                        break;
                                    case Detail:
                                        changeComparator(new DetailComparator());
                                        break;
                                }
                                return true;
                            }
                        })
                .positiveText(R.string.dialog_select_button)
                .show();
    }

    @Override
    public void onResult(List<Card> result) {
        if (abstractDeck == null) return;
        deck = new Deck();
        deck.setId(abstractDeck.getId());
        deck.setName(abstractDeck.getName());
        for (DeckRecord record : records) {
            for (Card card : result) {
                if (!Objects.equals(card.getSerial(), record.getSerial())) continue;
                deck.setCardCount(card, record.getCount());
                break;
            }
        }
        resetDeck();
    }

    private static class SerialComparator implements Comparator<Multiset.Entry<Card>> {
        @Override
        public int compare(Multiset.Entry<Card> o1, Multiset.Entry<Card> o2) {
            return o1.getElement().getSerial().compareTo(o2.getElement().getSerial());
        }
    }

    private static class LevelComparator implements Comparator<Multiset.Entry<Card>> {
        @Override
        public int compare(Multiset.Entry<Card> o1, Multiset.Entry<Card> o2) {
            return o1.getElement().getLevel() - o2.getElement().getLevel();
        }
    }

    private static class DetailComparator implements Comparator<Multiset.Entry<Card>> {
        @Override
        public int compare(Multiset.Entry<Card> o1, Multiset.Entry<Card> o2) {
            final Card left = o1.getElement();
            final Card right = o2.getElement();
            return ComparisonChain.start()
                    .compare(left.getColor().ordinal(), right.getColor().ordinal())
                    .compare(left.getType().ordinal(), right.getType().ordinal())
                    .compare(left.getLevel(), right.getLevel())
                    .compare(left.getSerial(), right.getSerial())
                    .result();
        }
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

        public List<Multiset.Entry<Card>> getModels() {
            return models;
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
                    final Fragment fragment = CardDetailFragment.newInstance(card);
                    fragment.setTargetFragment(DeckEditFragment.this, REQUEST_CARD_DETAIL);
                    ((MainActivity) getActivity()).transactTo(fragment, true);
                }
            });
            imageName = card.getImage();
            loadImage(card, this);
            nameTextView.setText(card.getName());
            serialTextView.setText(getString(R.string.format_card_detail, card.getSerial(),
                    card.getLevel(), getString(card.getType().getStringId())));
            colorView.setBackgroundResource(card.getColor().getColorId());
            countTextView.setText(String.valueOf(entry.getCount()));
            countTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showChangeCardCountDialog(entry);
                }
            });
            itemView.setOnLongClickListener(
                    new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            deck.setCover(entry.getElement().getSerial());
                            DeckRepository.updateDeck(getContext(), deck.toAbstract());
                            showMessage(R.string.msg_set_fav);
                            return true;
                        }
                    });
        }

        private void showChangeCardCountDialog(@NonNull final Multiset.Entry<Card> entry) {
            final Card card = entry.getElement();
            new MaterialDialog.Builder(getContext())
                    .title(R.string.dialog_change_card_count)
                    .content(card.getSerial() + " " + card.getName())
                    .inputType(InputType.TYPE_CLASS_NUMBER)
                    .positiveText(R.string.dialog_change_button)
                    .negativeText(R.string.dialog_cancel_button)
                    .input(getString(R.string.dialog_count), String.valueOf(entry.getCount()), false,
                            new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                    final int count = Integer.valueOf(input.toString());
                                    DeckRepository.updateDeckCount(getActivity(), deck.getId(), card.getSerial(), count);
                                }
                            })
                    .show();
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
