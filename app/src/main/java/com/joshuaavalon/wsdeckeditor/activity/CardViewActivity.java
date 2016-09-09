package com.joshuaavalon.wsdeckeditor.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.WsApplication;
import com.joshuaavalon.wsdeckeditor.model.Card;
import com.joshuaavalon.wsdeckeditor.model.Deck;
import com.joshuaavalon.wsdeckeditor.repository.CardRepository;
import com.joshuaavalon.wsdeckeditor.repository.DeckRepository;
import com.joshuaavalon.wsdeckeditor.view.CardPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Show a list of {@link Card} in {@link ViewPager}.
 */
public class CardViewActivity extends BaseActivity {
    private static final String ARG_SERIALS = "serials";
    private static final String ARG_POSITION = "position";
    private List<String> serials;

    /**
     * Use this method to start the activity.
     *
     * @param activity Current activity.
     * @param serials  Serials of the cards to be shown
     * @param position Starting position of the {@link Card}
     */
    public static void start(@NonNull final Activity activity,
                             @NonNull final List<String> serials,
                             @IntRange(from = 0) final int position) {
        final Intent intent = new Intent(activity, CardViewActivity.class);
        intent.putExtra(ARG_SERIALS, new ArrayList<>(serials));
        intent.putExtra(ARG_POSITION, position);
        // To start search fragment on activity.
        activity.startActivityForResult(intent, MainActivity.REQUEST_CODE_CARD_DETAIL);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_view);
        final Intent intent = getIntent();
        int position = 0;
        if (intent != null) {
            serials = intent.getStringArrayListExtra(ARG_SERIALS);
            final int intentPosition = intent.getIntExtra(ARG_POSITION, 0);
            if (intentPosition >= 0 && serials != null && intentPosition < serials.size())
                position = intentPosition;
        }
        if (serials == null)
            serials = new ArrayList<>();
        initViewPager(position);
    }

    private void initViewPager(final int startPosition) {
        final CardPagerAdapter adapter = new CardPagerAdapter(getSupportFragmentManager(), serials);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(adapter);
        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(startPosition);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeckSelectDialog(serials.get(viewPager.getCurrentItem()));
            }
        });
    }

    /**
     * Show a dialog to add a given serial to deck.
     *
     * @param serial Serial to be added.
     */
    private void showDeckSelectDialog(@NonNull final String serial) {
        final List<Deck> decks = DeckRepository.getDecks();
        new MaterialDialog.Builder(this)
                .iconRes(R.drawable.ic_assignment_black_24dp)
                .title(R.string.dialog_select_your_deck)
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
                        final Deck deck = decks.get(which);
                        showCardCountDialog(deck.getId(), serial);
                    }
                })
                .positiveText(R.string.dialog_new_button)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        showCreateDeckDialog(serial);
                    }
                })
                .show();
    }

    private void showCardCountDialog(final long deckId, @NonNull final String serial) {
        final Card card = CardRepository.getCardBySerial(serial).get();
        final Deck deck = DeckRepository.getDeckById(deckId).get();
        new MaterialDialog.Builder(this)
                .iconRes(R.drawable.ic_edit_black_24dp)
                .title(R.string.dialog_change_card_count)
                .content(serial + " " + card.getName())
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .positiveText(R.string.dialog_apply_button)
                .negativeText(R.string.dialog_cancel_button)
                .input(getString(R.string.dialog_count),
                        String.valueOf(deck.getSerialList().count(serial)),
                        false,
                        new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                final int count = Integer.valueOf(input.toString());
                                if (count > WsApplication.SINGLE_CARD_LIMIT) {
                                    showMessage(R.string.msg_high_card_count);
                                    return;
                                }
                                deck.setCount(card.getSerial(), count);
                                DeckRepository.save(deck);
                                showMessage(R.string.msg_add_to_deck);
                            }
                        })
                .show();
    }

    private void showCreateDeckDialog(@NonNull final String serial) {
        new MaterialDialog.Builder(this)
                .iconRes(R.drawable.ic_add_black_24dp)
                .title(R.string.dialog_create_a_new_deck)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .positiveText(R.string.dialog_create_button)
                .negativeText(R.string.dialog_cancel_button)
                .input(R.string.dialog_deck_name, 0, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        final Deck deck = new Deck();
                        deck.setName(input.toString());
                        DeckRepository.save(deck);
                        showCardCountDialog(deck.getId(), serial);
                    }
                })
                .show();
    }
}
