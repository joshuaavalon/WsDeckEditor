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
import com.joshuaavalon.wsdeckeditor.model.Deck;
import com.joshuaavalon.wsdeckeditor.repository.DeckRepository;
import com.joshuaavalon.wsdeckeditor.view.CardPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class CardViewActivity extends BaseActivity {
    private static final String ARG_SERIALS = "serials";
    private static final String ARG_POSITION = "position";
    private List<String> serials;

    public static void start(@NonNull final Activity context,
                             @NonNull final List<String> serials,
                             @IntRange(from = 0) final int position) {
        final Intent intent = new Intent(context, CardViewActivity.class);
        intent.putExtra(ARG_SERIALS, new ArrayList<>(serials));
        intent.putExtra(ARG_POSITION, position);
        context.startActivityForResult(intent, MainActivity.REQUEST_CODE_CARD_DETAIL);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_view);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        final Intent intent = getIntent();
        int position = 0;
        if (intent != null) {
            serials = intent.getStringArrayListExtra(ARG_SERIALS);
            final int intentPosition = intent.getIntExtra(ARG_POSITION, 0);
            if (intentPosition >= 0 && intentPosition < serials.size())
                position = intentPosition;
        }
        if (serials == null)
            serials = new ArrayList<>();
        final CardPagerAdapter adapter = new CardPagerAdapter(getSupportFragmentManager(), serials);
        viewPager.setAdapter(adapter);
        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(position);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeckSelectDialog(serials.get(viewPager.getCurrentItem()));
            }
        });
    }

    private void showDeckSelectDialog(@NonNull final String serial) {
        final List<Deck> decks = DeckRepository.getDecks();
        new MaterialDialog.Builder(this)
                .iconRes(R.drawable.ic_assignment_black_24dp)
                .title(R.string.select_your_deck)
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
                        final Deck currentDeck = decks.get(which);
                        currentDeck.addIfNotExist(serial);
                        DeckRepository.save(currentDeck);
                        dialog.dismiss();
                    }
                })
                .positiveText(R.string.new_deck)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        showCreateDeckDialog(dialog, serial);
                    }
                })
                .autoDismiss(false)
                .show();
    }

    private void showCreateDeckDialog(@NonNull final MaterialDialog parent,
                                      @NonNull final String serial) {
        new MaterialDialog.Builder(this)
                .iconRes(R.drawable.ic_add_black_24dp)
                .title(R.string.create_a_new_deck)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .positiveText(R.string.create_deck_create)
                .negativeText(R.string.cancel_button)
                .input(R.string.deck_name, 0, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        final Deck deck = new Deck();
                        deck.setName(input.toString());
                        deck.addIfNotExist(serial);
                        DeckRepository.save(deck);
                        parent.dismiss();
                        showMessage(R.string.add_to_deck);
                    }
                })
                .show();
    }
}
