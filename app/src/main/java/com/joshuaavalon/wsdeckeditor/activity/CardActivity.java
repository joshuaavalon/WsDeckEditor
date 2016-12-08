package com.joshuaavalon.wsdeckeditor.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.joshuaavalon.android.view.ContentView;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.config.Constant;
import com.joshuaavalon.wsdeckeditor.sdk.card.Card;
import com.joshuaavalon.wsdeckeditor.sdk.deck.DeckMeta;
import com.joshuaavalon.wsdeckeditor.view.DialogUtils;
import com.joshuaavalon.wsdeckeditor.view.tab.CardPagerAdapter;
import com.joshuaavalon.wsdeckeditor.view.tab.PositionListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

@ContentView(R.layout.activity_card)
public class CardActivity extends BaseActivity {
    public static final String ARG_SERIALS = "CardActivity.Serials";
    public static final String ARG_POSITION = "CardActivity.Position";
    public static final String ARG_DECK = "CardActivity.Deck";
    @BindView(R.id.tab_layout)
    TabLayout tabLayout;
    @BindView(R.id.view_pager)
    ViewPager viewPager;
    @BindView(R.id.fab)
    FloatingActionButton floatingActionButton;
    private CardPagerAdapter adapter;
    private PositionListener listener;

    public static void start(@NonNull final Context context, @NonNull final ArrayList<String> serials,
                             final int position, final boolean isDeck) {
        final Intent intent = new Intent(context, CardActivity.class);
        final Bundle args = new Bundle();
        args.putStringArrayList(ARG_SERIALS, serials);
        args.putInt(ARG_POSITION, position);
        args.putBoolean(ARG_DECK, isDeck);
        intent.putExtras(args);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        if (!intent.hasExtra(ARG_SERIALS)) {
            Timber.w("CardActivity: Empty argument");
            return;
        }
        int position = intent.getIntExtra(ARG_POSITION, 0);
        if (position < 0)
            position = 0;
        final List<String> serials = intent.getStringArrayListExtra(ARG_SERIALS);
        adapter = new CardPagerAdapter(getSupportFragmentManager(), serials);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        listener = new PositionListener();
        viewPager.setCurrentItem(position);
        viewPager.addOnPageChangeListener(listener);
        final ViewPager.SimpleOnPageChangeListener listener = new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                final String serial = serials.get(position);
                final Card card = getCardRepository().find(serial);
                if (card == null) return;
                setTitle(card.getName());
                if (toolbar == null) return;
                final int color = ContextCompat.getColor(CardActivity.this, card.getColor().getColorId());
                toolbar.setBackgroundColor(color);
            }
        };
        viewPager.addOnPageChangeListener(listener);
        listener.onPageSelected(position);
        final View view = tabLayout.getChildAt(0);
        if (view == null || !(view instanceof ViewGroup)) return;
        final ViewGroup tabs = (ViewGroup) view;
        for (int i = 0; i < serials.size(); i++) {
            final Card card = getCardRepository().find(serials.get(i));
            final View tab = tabs.getChildAt(i);
            if (tab == null || card == null) continue;
            final int color = ContextCompat.getColor(this, card.getColor().getColorId());
            tab.setBackgroundColor(color);
            if (position == i)
                tabLayout.setBackgroundColor(color);
        }
        final boolean isDeck = intent.getBooleanExtra(ARG_DECK, true);
        if (isDeck)
            coordinatorLayout.removeView(floatingActionButton);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                final Card card = getCardRepository().find(serials.get(position));
                if (card == null) return;
                final int color = ContextCompat.getColor(CardActivity.this, card.getColor().getColorId());
                tabLayout.setBackgroundColor(color);
            }
        });
    }

    @Override
    protected void initializeActionBar(@NonNull ActionBar actionBar) {
        super.initializeActionBar(actionBar);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @OnClick(R.id.fab)
    void addCard() {
        final List<DeckMeta> decks = getDeckRepository().meta();
        if (decks.size() <= 0) {
            Snackbar.make(coordinatorLayout, R.string.msg_no_deck, Snackbar.LENGTH_LONG)
                    .setAction(R.string.dialog_create_button, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DialogUtils.showCreateDeckDialog(CardActivity.this, getDeckRepository(), null);
                        }
                    })
                    .show();
            return;
        }
        final List<String> deckNames = Lists.newArrayList(Iterables.transform(decks,
                new Function<DeckMeta, String>() {
                    @Nullable
                    @Override
                    public String apply(DeckMeta input) {
                        return input.getName();
                    }
                }));
        final MaterialDialog.ListCallback callback = new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                final long id = decks.get(position).getId();
                if (checkTooManyCardInDeck(id)) return;
                final String serial = adapter.getSerials().get(listener.getCurrentPage());
                getDeckRepository().add(id, serial, getPreference().getAddIfNotExist());
                showMessage(R.string.msg_add_to_deck);
            }
        };
        DialogUtils.showDeckSelectDialog(this, deckNames, callback);
    }

    private boolean checkTooManyCardInDeck(long id) {
        final boolean result = getDeckRepository().cardCount(id) > Constant.DeckLimit;
        if (result)
            showMessage(R.string.msg_cards_deck);
        return result;
    }
}
