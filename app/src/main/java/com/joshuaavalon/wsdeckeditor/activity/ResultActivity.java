package com.joshuaavalon.wsdeckeditor.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.util.LruCache;
import android.support.v7.app.ActionBar;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.joshuaavalon.android.view.ContentView;
import com.joshuaavalon.android.view.recyclerview.BindingViewHolder;
import com.joshuaavalon.android.view.recyclerview.ViewHolderFactory;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.config.Constant;
import com.joshuaavalon.wsdeckeditor.sdk.card.Card;
import com.joshuaavalon.wsdeckeditor.sdk.card.Filter;
import com.joshuaavalon.wsdeckeditor.sdk.deck.Deck;
import com.joshuaavalon.wsdeckeditor.sdk.deck.DeckMeta;
import com.joshuaavalon.wsdeckeditor.task.CircularCardImageLoadTask;
import com.joshuaavalon.wsdeckeditor.util.ColorUtils;
import com.joshuaavalon.wsdeckeditor.view.ActionModeListener;
import com.joshuaavalon.wsdeckeditor.view.CardImageHolder;
import com.joshuaavalon.wsdeckeditor.view.DialogUtils;
import com.joshuaavalon.wsdeckeditor.view.recycler.adapter.SelectableAdapter;
import com.joshuaavalon.wsdeckeditor.view.recycler.viewholder.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import timber.log.Timber;

@ContentView(R.layout.activity_result)
public class ResultActivity extends BaseActivity implements ActionModeListener, ActionMode.Callback,
        SearchView.OnQueryTextListener {
    private static final String ARG_FILTER = "ResultActivity.Filter";
    private static final String ARG_EXPANSION = "ResultActivity.Expansion";
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @Nullable
    private ActionMode actionMode;
    private SelectableAdapter<Card> adapter;
    private List<Card> cards;
    private int selectedPosition = -1;
    private LruCache<String, Drawable> drawableLruCache;

    public static void start(@NonNull final Context context, @Nullable Filter filter) {
        final Intent intent = new Intent(context, ResultActivity.class);
        final Bundle args = new Bundle();
        if (filter != null)
            args.putParcelable(ARG_FILTER, filter);
        intent.putExtras(args);
        context.startActivity(intent);
    }

    public static void start(@NonNull final Context context, @NonNull final String expansion) {
        final Intent intent = new Intent(context, ResultActivity.class);
        final Bundle args = new Bundle();
        args.putString(ARG_EXPANSION, expansion);
        intent.putExtras(args);
        context.startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        drawableLruCache.evictAll();
    }

    private void updateSelectedPosition() {
        final long id = getPreference().getSelectedDeck();
        if (id < 0) return;
        selectedPosition = Iterables.indexOf(getDeckRepository().meta(), new Predicate<DeckMeta>() {
            @Override
            public boolean apply(@Nullable DeckMeta input) {
                return input != null && input.getId() == id;
            }
        });
    }

    //region ActionModeListener
    @Override
    public void onItemClicked(final int position) {
        toggleSelection(position);
    }

    @Override
    public boolean onItemLongClicked(final int position) {
        if (actionMode == null)
            actionMode = startSupportActionMode(this);
        toggleSelection(position);
        return true;
    }

    //region ActionMode.Callback
    @Override
    public boolean onCreateActionMode(final ActionMode actionMode, final Menu menu) {
        actionMode.getMenuInflater().inflate(R.menu.action_mode_card_list, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(final ActionMode actionMode, final Menu menu) {
        return false;
    }
    //endregion

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

    @Override
    public void onDestroyActionMode(final ActionMode actionMode) {
        adapter.clearSelection();
        this.actionMode = null;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(final String query) {
        final List<Card> filteredCardList = Lists.newArrayList(Iterables.filter(cards, new Predicate<Card>() {
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
    //endregion
    //region SearchView.OnQueryTextListener

    private void selectDeckDialog() {
        final List<DeckMeta> decks = getDeckRepository().meta();
        if (decks.size() <= 0) {
            showNoDeckFound();
            return;
        }
        DialogUtils.showDeckSelectDialog(this, getDeckName(decks),
                selectedPosition,
                new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        selectedPosition = which;
                        getPreference().setSelectedDeck(decks.get(which).getId());
                        return true;
                    }
                });
    }

    private void addSelectedCard() {
        if (adapter.getSelectedItemCount() > Constant.SelectCardLimit) {
            showMessage(R.string.msg_select_too_many);
            return;
        }
        final List<DeckMeta> decks = getDeckRepository().meta();
        if (decks.size() <= 0) {
            showNoDeckFound();
            return;
        }
        DialogUtils.showDeckSelectDialog(this, getDeckName(decks),
                new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        final long id = decks.get(which).getId();
                        if (checkTooManyCardInDeck(id)) return;
                        for (int index : adapter.getSelectedItems())
                            getDeckRepository().add(id, cards.get(index).getSerial(), getPreference().getAddIfNotExist());
                        showMessage(R.string.msg_add_to_deck);
                        if (actionMode != null && getPreference().getAutoClose())
                            actionMode.finish();
                    }
                });
    }
    //endregion

    private List<String> getDeckName(List<DeckMeta> decks) {
        return Lists.newArrayList(Iterables.transform(decks, new Function<DeckMeta, String>() {
            @Nullable
            @Override
            public String apply(DeckMeta input) {
                return input.getName();
            }
        }));
    }

    private void showNoDeckFound() {
        Snackbar.make(coordinatorLayout, R.string.msg_no_deck, Snackbar.LENGTH_LONG)
                .setAction(R.string.dialog_create_button, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogUtils.showCreateDeckDialog(
                                ResultActivity.this,
                                getDeckRepository(),
                                new DialogUtils.CreateDeckCallback() {
                                    @Override
                                    public void onCreate(@NonNull Deck deck) {
                                        getPreference().setSelectedDeck(deck.getId());
                                        updateSelectedPosition();
                                    }
                                });
                    }
                })
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.action_add:
                selectDeckDialog();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        if (!intent.hasExtra(ARG_EXPANSION) && !intent.hasExtra(ARG_FILTER)) {
            Timber.w("ResultActivity: Empty argument");
            return;
        }
        drawableLruCache = new LruCache<>(Constant.DrawableCache);
        Filter filter;
        if (intent.hasExtra(ARG_EXPANSION)) {
            final String title = intent.getStringExtra(ARG_EXPANSION);
            filter = new Filter();
            filter.setExpansion(title);
            filter.setNormalOnly(getPreference().getHideNormal());
            setTitle(title);
        } else {
            filter = intent.getParcelableExtra(ARG_FILTER);
            setTitle(R.string.search_result);
        }
        cards = getCardRepository().findAll(filter, getPreference().getShowLimit(), 0);
        adapter = new SelectableAdapter<>(cards, new CardViewHolderFactory());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        updateSelectedPosition();
    }

    @Override
    protected void initializeActionBar(@NonNull ActionBar actionBar) {
        super.initializeActionBar(actionBar);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_card_list, menu);
        return true;
    }

    private void toggleSelection(final int position) {
        if (actionMode == null) return;
        adapter.toggleSelection(position);
        final int count = adapter.getSelectedItemCount();
        if (count == 0 && getPreference().getAutoClose()) {
            actionMode.finish();
        } else {
            actionMode.invalidate();
        }
    }

    private void addCardToDeck(@NonNull final Card card) {
        final List<DeckMeta> decks = getDeckRepository().meta();
        if (decks.size() == 0) {
            showNoDeckFound();
            return;
        }
        if (selectedPosition < 0 || selectedPosition >= decks.size()) {
            showMessage(R.string.msg_no_select_deck);
            return;
        }
        final long id = decks.get(selectedPosition).getId();
        if (checkTooManyCardInDeck(id)) return;
        getDeckRepository().add(id, card.getSerial(), getPreference().getAddIfNotExist());
        showMessage(R.string.msg_add_to_deck);
    }

    private boolean checkTooManyCardInDeck(long id) {
        final boolean result = getDeckRepository().cardCount(id) > Constant.DeckLimit;
        if (result)
            showMessage(R.string.msg_cards_deck);
        return result;
    }

    class CardViewHolder extends BaseViewHolder<Card> implements View.OnLongClickListener,
            CardImageHolder {
        @Nullable
        private final ActionModeListener actionModeListener;
        @BindView(R.id.card_image)
        ImageView imageView;
        @BindView(R.id.card_name)
        TextView nameTextView;
        @BindView(R.id.card_serial)
        TextView serialTextView;
        @BindView(R.id.card_background)
        RelativeLayout relativeLayout;
        @BindView(R.id.color_bar)
        View colorView;
        @BindView(R.id.image_view)
        ImageView actionView;
        @NonNull
        private String imageName;

        public CardViewHolder(@NonNull final View itemView, @Nullable final ActionModeListener actionModeListener) {
            super(itemView);
            itemView.setOnLongClickListener(this);
            this.actionModeListener = actionModeListener;
            imageName = "";
        }

        @Override
        public void bind(final Card card) {
            relativeLayout.setActivated(adapter.isSelected(getAdapterPosition()));
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (actionMode == null) {
                        final ArrayList<String> serials = Lists.newArrayList(
                                Iterables.transform(cards, new Function<Card, String>() {
                                    @Override
                                    public String apply(Card input) {
                                        return input.getSerial();
                                    }
                                })
                        );
                        CardActivity.start(ResultActivity.this, serials, serials.indexOf(card.getSerial()), false);
                    } else {
                        if (actionModeListener == null) return;
                        actionModeListener.onItemClicked(getAdapterPosition());
                    }
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
            serialTextView.setText(getString(R.string.format_card_detail, card.getSerial(),
                    card.getLevel(), getString(card.getType().getStringId())));
            colorView.setBackgroundResource(card.getColor().getColorId());
            relativeLayout.setBackgroundResource(ColorUtils.getBackgroundDrawable(card.getColor()));
            actionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addCardToDeck(card);
                }
            });
        }

        @Override
        public boolean onLongClick(View view) {
            return actionModeListener != null && actionModeListener.onItemLongClicked(getAdapterPosition());
        }

        @Override
        public void setImage(@NonNull Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
        }

        @Override
        public void setImage(@NonNull Drawable drawable) {
            imageView.setImageDrawable(drawable);
            drawableLruCache.put(getImageName(), drawable);
        }

        @NonNull
        @Override
        public String getImageName() {
            return imageName;
        }
    }

    private class CardViewHolderFactory extends ViewHolderFactory<Card> {
        @Override
        protected BindingViewHolder<Card> createViewHolder(View view, int viewType) {
            return new CardViewHolder(view, ResultActivity.this);
        }

        @Override
        protected int getLayoutId(int viewType) {
            return R.layout.list_item_card;
        }
    }
}
