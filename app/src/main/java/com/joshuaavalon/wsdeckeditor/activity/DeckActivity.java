package com.joshuaavalon.wsdeckeditor.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.LruCache;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.joshuaavalon.android.view.ContentView;
import com.joshuaavalon.android.view.recyclerview.AnimatedRecyclerAdapter;
import com.joshuaavalon.android.view.recyclerview.BindingViewHolder;
import com.joshuaavalon.android.view.recyclerview.ViewHolderFactory;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.config.CardOrder;
import com.joshuaavalon.wsdeckeditor.config.Constant;
import com.joshuaavalon.wsdeckeditor.sdk.card.Card;
import com.joshuaavalon.wsdeckeditor.sdk.deck.Deck;
import com.joshuaavalon.wsdeckeditor.task.CircularCardImageLoadTask;
import com.joshuaavalon.wsdeckeditor.view.CardImageHolder;
import com.joshuaavalon.wsdeckeditor.view.DialogUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

@ContentView(R.layout.activity_deck)
public class DeckActivity extends BaseActivity {
    public static final String ARG_ID = "DeckActivity.Id";
    private static final int REQ_CARD = 2;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    private Deck deck;
    private LruCache<String, Drawable> drawableLruCache;
    private Comparator<Multiset.Entry<Card>> comparator;
    private AnimatedRecyclerAdapter<Multiset.Entry<Card>> adapter;

    private void initializeSwipeRemove() {
        if (!getPreference().getSwipeRemove()) return;
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
                        getDeckRepository().update(deck.getId(), entry.getElement().getSerial(), 0);
                        Snackbar.make(coordinatorLayout, R.string.msg_remove_card, Snackbar.LENGTH_LONG)
                                .setAction(R.string.dialog_undo_button, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        getDeckRepository().update(deck.getId(), entry.getElement().getSerial(), entry.getCount());
                                        reload();
                                    }
                                })
                                .show();
                        reload();
                    }
                });
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void showSortDialog() {
        final MaterialDialog.ListCallbackSingleChoice callback =
                new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog,
                                               View itemView,
                                               int which,
                                               CharSequence text) {
                        final CardOrder order = CardOrder.values()[which];
                        getPreference().setSortOrder(order);
                        changeComparator(order.getComparator());
                        return true;
                    }
                };
        new MaterialDialog.Builder(this)
                .title(R.string.dialog_sort_by)
                .items(R.array.sort_type)
                .itemsCallbackSingleChoice(getPreference().getSortOrder().ordinal(), callback)
                .positiveText(R.string.dialog_sort_button)
                .negativeText(R.string.dialog_cancel_button)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_deck_edit, menu);
        return true;
    }

    private void setTitle() {
        setTitle(deck.getName());
    }

    private void reload() {
        setTitle();
        resetDeck();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.action_info:
                DialogUtils.showDeckInfoDialog(this, deck);
                return true;
            case R.id.action_sort:
                showSortDialog();
                return true;
            case R.id.action_delete:
                removeDeck();
                return true;
            case R.id.action_copy:
                deck.setId(Deck.NO_ID);
                getDeckRepository().save(deck);
                showMessage(R.string.msg_deck_duplicated);
                return true;
            case R.id.action_image:
                DeckImageActivity.start(this, deck.getId(), coordinatorLayout);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long id = getIntent().getLongExtra(ARG_ID, -1);
        if (id < 0) {
            Timber.w("DeckActivity: Empty Argument");
            finish();
            return;
        }
        deck = getDeckRepository().deck(id);
        setTitle();
        drawableLruCache = new LruCache<>(Constant.DrawableCache);
        comparator = getPreference().getSortOrder().getComparator();
        final List<Multiset.Entry<Card>> cardEntries = Lists.newArrayList(deck.getCardList().entrySet());
        Collections.sort(cardEntries, comparator);
        adapter = new AnimatedRecyclerAdapter<>(cardEntries,
                new ViewHolderFactory<Multiset.Entry<Card>>() {
                    @Override
                    protected BindingViewHolder<Multiset.Entry<Card>> createViewHolder(View view, int viewType) {
                        return new CardViewHolder(view);
                    }

                    @Override
                    protected int getLayoutId(int viewType) {
                        return R.layout.list_item_deck_edit;
                    }
                });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        initializeSwipeRemove();
    }

    @Override
    protected void initializeActionBar(@NonNull ActionBar actionBar) {
        super.initializeActionBar(actionBar);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void removeDeck() {
        new MaterialDialog.Builder(this)
                .title(R.string.dialog_delete_deck)
                .positiveText(R.string.dialog_delete_button)
                .negativeText(R.string.dialog_cancel_button)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        getDeckRepository().remove(deck);
                        finish();
                    }
                })
                .show();
    }

    private void resetDeck() {
        deck = getDeckRepository().deck(deck.getId());
        final List<Multiset.Entry<Card>> cardEntries = Lists.newArrayList(deck.getCardList().entrySet());
        Collections.sort(cardEntries, comparator);
        adapter.setModels(cardEntries);
        recyclerView.scrollToPosition(0);
    }

    private void changeComparator(@NonNull final Comparator<Multiset.Entry<Card>> comparator) {
        this.comparator = comparator;
        resetDeck();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQ_CARD) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        reload();
    }

    class CardViewHolder extends BindingViewHolder<Multiset.Entry<Card>>
            implements CardImageHolder {
        //region Views
        @BindView(R.id.card_image)
        ImageView imageView;
        @BindView(R.id.card_name)
        TextView nameTextView;
        @BindView(R.id.card_serial)
        TextView serialTextView;
        @BindView(R.id.card_count)
        TextView countTextView;
        @BindView(R.id.color_bar)
        View colorView;
        //endregion
        @NonNull
        private String imageName;

        public CardViewHolder(@NonNull final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            imageName = "";
        }

        @Override
        public void bind(final Multiset.Entry<Card> entry) {
            final Card card = entry.getElement();
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Intent intent = new Intent(DeckActivity.this, CardActivity.class);
                    final Bundle args = new Bundle();
                    final ArrayList<String> serial = Lists.newArrayList(Iterables.transform(adapter.getModels(),
                            new Function<Multiset.Entry<Card>, String>() {
                                @Override
                                public String apply(Multiset.Entry<Card> input) {
                                    return input.getElement().getSerial();
                                }
                            }));
                    args.putStringArrayList(CardActivity.ARG_SERIALS, serial);
                    args.putInt(CardActivity.ARG_POSITION, getAdapterPosition());
                    args.putBoolean(CardActivity.ARG_DECK, true);
                    intent.putExtras(args);
                    startActivityForResult(intent, REQ_CARD);
                }
            });
            imageView.setImageBitmap(null);
            imageName = card.getImage();
            final Drawable drawable = drawableLruCache.get(imageName);
            if (drawable != null)
                imageView.setImageDrawable(drawable);
            else
                new CircularCardImageLoadTask(getCardRepository(), this, card).execute(getResources());
            nameTextView.setText(card.getName());
            final String detailString = getString(R.string.format_card_detail, card.getSerial(),
                    card.getLevel(), getString(card.getType().getStringId()));
            if (!Objects.equal(deck.getCover(), card.getSerial()))
                serialTextView.setText(detailString);
            else {
                final String star = "★";
                final SpannableStringBuilder builder = new SpannableStringBuilder();
                builder.append(detailString);
                builder.append(" ");
                final SpannableString starSpannable = new SpannableString(star);
                final int color = ContextCompat.getColor(DeckActivity.this, R.color.highlight_icon);
                starSpannable.setSpan(new ForegroundColorSpan(color), 0, star.length(), 0);
                builder.append(starSpannable);
                serialTextView.setText(builder, TextView.BufferType.SPANNABLE);
            }
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
                            final String previousCover = deck.getCover();
                            deck.setCover(entry.getElement().getSerial());
                            getDeckRepository().save(deck.meta());
                            adapter.notifyItemChanged(getAdapterPosition());
                            final int previousIndex = Iterables.indexOf(adapter.getModels(),
                                    new Predicate<Multiset.Entry<Card>>() {
                                        @Override
                                        public boolean apply(Multiset.Entry<Card> input) {
                                            return Objects.equal(input.getElement().getSerial(), previousCover);
                                        }
                                    });
                            if (previousIndex >= 0)
                                adapter.notifyItemChanged(previousIndex);
                            showMessage(R.string.msg_set_fav);
                            return true;
                        }
                    });
        }

        private void showChangeCardCountDialog(@NonNull final Multiset.Entry<Card> entry) {
            final Card card = entry.getElement();
            new MaterialDialog.Builder(DeckActivity.this)
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
                                    getDeckRepository().update(deck.getId(), card.getSerial(), count);
                                    reload();
                                }
                            })
                    .show();
        }

        @Override
        public void setImage(@NonNull Bitmap bitmap) {
            if (imageView != null)
                imageView.setImageBitmap(bitmap);
        }

        @Override
        public void setImage(@NonNull Drawable drawable) {
            if (imageView != null)
                imageView.setImageDrawable(drawable);
        }

        @NonNull
        @Override
        public String getImageName() {
            return imageName;
        }
    }
}
