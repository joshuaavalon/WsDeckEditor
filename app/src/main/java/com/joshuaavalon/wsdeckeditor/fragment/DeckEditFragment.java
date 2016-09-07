package com.joshuaavalon.wsdeckeditor.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.Utility;
import com.joshuaavalon.wsdeckeditor.activity.CardViewActivity;
import com.joshuaavalon.wsdeckeditor.activity.MainActivity;
import com.joshuaavalon.wsdeckeditor.model.Card;
import com.joshuaavalon.wsdeckeditor.model.Deck;
import com.joshuaavalon.wsdeckeditor.model.DeckUtils;
import com.joshuaavalon.wsdeckeditor.model.QRCode;
import com.joshuaavalon.wsdeckeditor.repository.CardRepository;
import com.joshuaavalon.wsdeckeditor.repository.DeckRepository;
import com.joshuaavalon.wsdeckeditor.repository.PreferenceRepository;
import com.joshuaavalon.wsdeckeditor.view.BaseRecyclerViewHolder;
import com.joshuaavalon.wsdeckeditor.view.ColorUtils;
import com.joshuaavalon.wsdeckeditor.view.SelectableAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DeckEditFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String ARG_DECK_ID = "deckId";
    private static final String INFO_DIALOG_SEPARATOR = "\n";
    private static final int QR_SIZE = 1000;
    private CardRecyclerViewAdapter adapter;
    private Deck deck;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View view;
    private Uri qrUri = null;
    private boolean isChanged = false;

    public static DeckEditFragment newInstance(final long deckId) {
        final DeckEditFragment fragment = new DeckEditFragment();
        final Bundle args = new Bundle();
        args.putLong(ARG_DECK_ID, deckId);
        fragment.setArguments(args);
        return fragment;
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

    private void showSortDialog() {
        new MaterialDialog.Builder(getContext())
                .title(R.string.sort)
                .iconRes(R.drawable.ic_sort_black_24dp)
                .items(R.array.sort_type)
                .itemsCallbackSingleChoice(PreferenceRepository.getSortOrder().toInt(),
                        new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog,
                                                       View itemView,
                                                       int which,
                                                       CharSequence text) {
                                final Card.SortOrder order = Card.SortOrder.fromInt(which);
                                PreferenceRepository.setSortOrder(order);
                                sort(order);
                                return true;
                            }
                        })
                .positiveText(R.string.select_button)
                .negativeText(R.string.cancel_button)
                .show();
    }

    private void showRenameDialog(@NonNull final Deck deck) {
        new MaterialDialog.Builder(getContext())
                .iconRes(R.drawable.ic_edit_black_24dp)
                .title(R.string.rename_deck)
                .content(deck.getName())
                .inputType(InputType.TYPE_CLASS_TEXT)
                .positiveText(R.string.rename_button)
                .negativeText(R.string.cancel_button)
                .input(getString(R.string.deck_name), deck.getName(), false,
                        new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                deck.setName(input.toString());
                                isChanged = true;
                                refresh();
                            }
                        })
                .show();
    }

    private void showChangeCardCountDialog(@NonNull final Multiset.Entry<Card> entry) {
        final Card card = entry.getElement();
        new MaterialDialog.Builder(getContext())
                .iconRes(R.drawable.ic_edit_black_24dp)
                .title(R.string.change_card_count)
                .content(card.getSerial() + " " + card.getName())
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .positiveText(R.string.change_button)
                .negativeText(R.string.cancel_button)
                .input(getString(R.string.count), String.valueOf(entry.getCount()), false,
                        new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                deck.setCount(card.getSerial(),
                                        Integer.valueOf(input.toString()));
                                isChanged = true;
                                refresh();
                            }
                        })
                .show();
    }

    private void showDeckInfoDialog() {
        final MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                .title(R.string.deck_info)
                .iconRes(R.drawable.ic_info_outline_black_24dp)
                .customView(R.layout.dialog_deck_info, true)
                .show();
        final View view = dialog.getCustomView();
        if (view == null) return;
        final TextView expansionTextView = (TextView) view.findViewById(R.id.expansion_content_text_view);
        expansionTextView.setText(Joiner.on(INFO_DIALOG_SEPARATOR).join(deck.getExpansions()));
        final Multiset<Card.Color> colorCount = HashMultiset.create();
        final Multiset<Card.Type> typeCount = HashMultiset.create();
        final Multiset<Integer> levelCount = HashMultiset.create();
        for (Card card : deck.getList()) {
            colorCount.add(card.getColor());
            typeCount.add(card.getType());
            levelCount.add(card.getLevel());
        }
        final TextView totalTextView = (TextView) view.findViewById(R.id.total_content_text_view);
        totalTextView.setText(DeckUtils.getStatusLabel(deck), TextView.BufferType.SPANNABLE);
        final List<String> tempList = new ArrayList<>();
        final TextView colorTextView = (TextView) view.findViewById(R.id.color_content_text_view);
        for (Card.Color color : Card.Color.values()) {
            if (colorCount.contains(color))
                tempList.add(getString(R.string.info_string, getString(color.getResId()), colorCount.count(color)));
        }
        colorTextView.setText(Joiner.on(INFO_DIALOG_SEPARATOR).join(tempList));
        tempList.clear();
        final TextView typeTextView = (TextView) view.findViewById(R.id.type_content_text_view);
        for (Card.Type type : Card.Type.values()) {
            if (typeCount.contains(type))
                tempList.add(getString(R.string.info_string, getString(type.getResId()), typeCount.count(type)));
        }
        typeTextView.setText(Joiner.on(INFO_DIALOG_SEPARATOR).join(tempList));
        tempList.clear();
        final TextView levelTextView = (TextView) view.findViewById(R.id.level_content_text_view);
        for (Integer level : Ordering.natural().sortedCopy(levelCount.elementSet())) {
            tempList.add(getString(R.string.info_level_string, String.valueOf(level), levelCount.count(level)));
        }
        levelTextView.setText(Joiner.on(INFO_DIALOG_SEPARATOR).join(tempList));
    }

    private void showDeckShareDialog() {
        final MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                .title(R.string.share_deck)
                .iconRes(R.drawable.ic_menu_share)
                .customView(R.layout.dialog_share, false)
                .positiveText(R.string.share_button)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (qrUri == null) return;
                        Utility.sharePublicBitmap(getActivity(), qrUri);
                        qrUri = null;
                    }
                })
                .show();
        final View view = dialog.getCustomView();
        if (view == null) return;
        final ImageView qrImageView = (ImageView) view.findViewById(R.id.image_view);
        final Bitmap qrBitmap = QRCode.encode(DeckUtils.encodeDeck(deck), QR_SIZE, QR_SIZE);
        qrImageView.setImageBitmap(qrBitmap);
        qrUri = Utility.savePublicBitmap(qrBitmap, "QR");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_sort:
                showSortDialog();
                return true;
            case R.id.menu_delete:
                DeckRepository.delete(deck);
                getFragmentManager().popBackStack();
                return true;
            case R.id.menu_info:
                showDeckInfoDialog();
                return true;
            case R.id.menu_share:
                showDeckShareDialog();
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
        view = inflater.inflate(R.layout.fragment_deck_edit, container, false);
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
                        isChanged = true;
                        refresh();
                    }
                });
        itemTouchHelper.attachToRecyclerView(recyclerView);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        setHasOptionsMenu(true);
        return view;
    }

    private void refresh() {
        final Activity activity = getActivity();
        if (activity instanceof MainActivity)
            ((MainActivity) activity).setTitle(deck.getName());
        sort(PreferenceRepository.getSortOrder());
        if (PreferenceRepository.getAutoSave() && isChanged)
            DeckRepository.save(deck);
        ColorUtils.setColorView(deck, view);
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
        final FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        if (fab == null || PreferenceRepository.getAutoSave()) return;
        fab.show();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeckRepository.save(deck);
                showMessage(R.string.deck_saved);
            }
        });
        fab.setImageResource(R.drawable.ic_save_white_24dp);
        final Activity activity = getActivity();
        if (activity instanceof MainActivity)
            ((MainActivity) activity).setToolbarOnClick(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showRenameDialog(deck);
                }
            });
    }

    @Override
    public void onPause() {
        super.onPause();
        final FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        if (fab != null)
            fab.hide();

        final Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).removeTitle();
            ((MainActivity) activity).setToolbarOnClick(null);
        }
    }

    private void sort(Card.SortOrder order) {
        final List<Multiset.Entry<Card>> lists = Lists.newArrayList(deck.getList().entrySet());
        Collections.sort(lists, transformComparator(Card.Comparator(order)));
        adapter.setModels(lists);
    }

    @Override
    public void onRefresh() {
        final Optional<Deck> deckOptional = DeckRepository.getDeckById(this.deck.getId());
        if (deckOptional.isPresent())
            this.deck = deckOptional.get();
        else
            this.deck.setId(Deck.NO_ID);
        refresh();
        swipeRefreshLayout.setRefreshing(false);
    }

    private class CardRecyclerViewAdapter extends SelectableAdapter<Multiset.Entry<Card>, CardViewHolder> {
        public CardRecyclerViewAdapter(@NonNull final Multiset<Card> items) {
            super(Lists.newArrayList(items.entrySet()));
        }

        @Override
        public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_edit, parent, false);
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
                    CardViewActivity.start(getActivity(), Lists.newArrayList(
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
            colorView.setBackgroundResource(card.getColor().getColorResId());
            linearLayout.setBackgroundResource(ColorUtils.getBackgroundDrawable(card.getColor()));
            countTextView.setText(String.valueOf(entry.getCount()));
            countTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showChangeCardCountDialog(entry);
                }
            });
            if (card.getType() != Card.Type.Climax)
                typeTextView.setText(getString(R.string.card_detail_prefix, card.getLevel(),
                        getString(card.getType().getResId())));
            else
                typeTextView.setText(getString(R.string.card_detail_prefix_climax,
                        getString(card.getType().getResId())));
            typeTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showChangeCardCountDialog(entry);
                }
            });
        }
    }
}

