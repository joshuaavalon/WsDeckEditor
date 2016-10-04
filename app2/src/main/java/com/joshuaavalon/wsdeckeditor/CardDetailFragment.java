package com.joshuaavalon.wsdeckeditor;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.joshuaavalon.wsdeckeditor.sdk.Card;
import com.joshuaavalon.wsdeckeditor.sdk.data.CardRepository;
import com.joshuaavalon.wsdeckeditor.sdk.data.DeckRepository;
import com.joshuaavalon.wsdeckeditor.sdk.util.AbstractDeck;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.app.Activity.RESULT_OK;

public class CardDetailFragment extends Fragment implements CardImageHolder,
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String ARG_CARD = "CardDetailFragment.arg.Card";
    public static final String RESULT_FILTER = "CardDetailFragment.extra.Filter";
    public static final String RESULT_TITLE = "CardDetailFragment.extra.Title";
    private ImageView imageView;
    private TextView nameTextView, serialTextView, expansionTextView, rarityTextView, sideImageView,
            typeTextView, colorTextView, levelTextView, costTextView, powerTextView, soulTextView,
            triggerTextView, attributeTextView, textTextView, flavorTextView;
    private Card card;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_card_detail, container, false);
        imageView = (ImageView) rootView.findViewById(R.id.card_detail_image);
        nameTextView = (TextView) rootView.findViewById(R.id.card_detail_name);
        serialTextView = (TextView) rootView.findViewById(R.id.card_detail_serial);
        expansionTextView = (TextView) rootView.findViewById(R.id.card_detail_expansion);
        rarityTextView = (TextView) rootView.findViewById(R.id.card_detail_rarity);
        sideImageView = (TextView) rootView.findViewById(R.id.card_detail_side);
        typeTextView = (TextView) rootView.findViewById(R.id.card_detail_type);
        colorTextView = (TextView) rootView.findViewById(R.id.card_detail_color);
        levelTextView = (TextView) rootView.findViewById(R.id.card_detail_level);
        costTextView = (TextView) rootView.findViewById(R.id.card_detail_cost);
        powerTextView = (TextView) rootView.findViewById(R.id.card_detail_power);
        soulTextView = (TextView) rootView.findViewById(R.id.card_detail_soul);
        triggerTextView = (TextView) rootView.findViewById(R.id.card_detail_trigger);
        attributeTextView = (TextView) rootView.findViewById(R.id.card_detail_attribute);
        textTextView = (TextView) rootView.findViewById(R.id.card_detail_text);
        flavorTextView = (TextView) rootView.findViewById(R.id.card_detail_flavor_text);
        card = getArguments().getParcelable(ARG_CARD);
        if (card == null) throw new IllegalArgumentException();
        bind(card);
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.action_add:
                getActivity().getSupportLoaderManager().initLoader(LoaderId.DeckListLoader, getArguments(), this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_card_detail, menu);
    }

    @NonNull
    public static CardDetailFragment newInstance(@NonNull final Card card) {
        final CardDetailFragment fragment = new CardDetailFragment();
        final Bundle args = new Bundle();
        args.putParcelable(ARG_CARD, card);
        fragment.setArguments(args);
        return fragment;
    }

    public void bind(@NonNull final Card card) {
        imageView.setImageBitmap(CardRepository.getImage(getContext(), card));
        nameTextView.setText(card.getName());
        serialTextView.setText(card.getSerial());
        expansionTextView.setText(card.getExpansion());
        rarityTextView.setText(card.getRarity());
        sideImageView.setText(card.getSide().getStringId());
        typeTextView.setText(card.getType().getStringId());
        colorTextView.setText(card.getColor().getStringId());
        if (card.getType() != Card.Type.Climax) {
            levelTextView.setText(String.valueOf(card.getLevel()));
            costTextView.setText(String.valueOf(card.getCost()));
        } else {
            levelTextView.setText(R.string.not_applicable_value);
            costTextView.setText(R.string.not_applicable_value);
        }
        if (card.getType() == Card.Type.Character) {
            powerTextView.setText(String.valueOf(card.getPower()));
            soulTextView.setText(String.valueOf(card.getSoul()));
        } else {
            powerTextView.setText(R.string.not_applicable_value);
            soulTextView.setText(R.string.not_applicable_value);
        }
        triggerTextView.setText(card.getTrigger().getStringId());
        final String first = card.getAttribute1();
        SpannableString firstSpan = new SpannableString(first);
        firstSpan.setSpan(getCharaSpan(first), 0, first.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        final String second = card.getAttribute2();
        if (second.equals(""))
            attributeTextView.setText(firstSpan);
        else {
            SpannableString secondSpan = new SpannableString(second);
            secondSpan.setSpan(getCharaSpan(second), 0, second.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            attributeTextView.setText(TextUtils.concat(firstSpan, "・", secondSpan));
        }
        attributeTextView.setMovementMethod(LinkMovementMethod.getInstance());
        String cardText = card.getText();
        SpannableStringBuilder spannableStringBuilder = regexSpan(new SpannableStringBuilder(cardText),
                "「.*?」",
                new Function<String, ClickableSpan>() {
                    @Override
                    public ClickableSpan apply(String arg) {
                        return getNameSpan(arg);
                    }
                });
        spannableStringBuilder = regexSpan(spannableStringBuilder, "《.*?》",
                new Function<String, ClickableSpan>() {
                    @Override
                    public ClickableSpan apply(String arg) {
                        return getCharaSpan(arg);
                    }
                });
        textTextView.setText(spannableStringBuilder);
        textTextView.setMovementMethod(LinkMovementMethod.getInstance());
        flavorTextView.setText(card.getFlavor());
    }

    private ClickableSpan getCharaSpan(@NonNull final String chara) {
        return new ClickableSpan() {
            @Override
            public void onClick(View view) {
                final CardRepository.Filter cardFilter = new CardRepository.Filter();
                cardFilter.setHasName(false);
                cardFilter.setHasChara(true);
                cardFilter.setHasSerial(false);
                cardFilter.setHasText(false);
                final Set<String> keywords = new HashSet<>();
                keywords.add(chara);
                cardFilter.setKeyword(keywords);
                startSearch(chara, cardFilter);
            }

            @Override
            public void updateDrawState(TextPaint textPaint) {
                super.updateDrawState(textPaint);
                textPaint.setUnderlineText(false);
            }
        };
    }


    private ClickableSpan getNameSpan(@NonNull final String name) {
        return new ClickableSpan() {
            @Override
            public void onClick(View view) {
                final CardRepository.Filter cardFilter = new CardRepository.Filter();
                cardFilter.setHasName(true);
                cardFilter.setHasChara(false);
                cardFilter.setHasSerial(false);
                cardFilter.setHasText(false);
                final Set<String> keywords = new HashSet<>();
                keywords.add(name);
                cardFilter.setKeyword(keywords);
                startSearch(name, cardFilter);
            }

            @Override
            public void updateDrawState(TextPaint textPaint) {
                super.updateDrawState(textPaint);
                textPaint.setUnderlineText(false);
            }
        };
    }


    private SpannableStringBuilder regexSpan(@NonNull final SpannableStringBuilder spannableStringBuilder,
                                             @NonNull final String regex,
                                             @NonNull final Function<String, ClickableSpan> factoryMethod) {
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(spannableStringBuilder);
        while (matcher.find()) {
            int start = matcher.start() + 1;
            int end = matcher.end() - 1;
            String text = spannableStringBuilder.subSequence(start, end).toString();
            ClickableSpan clickableSpan = factoryMethod.apply(text);
            spannableStringBuilder.setSpan(clickableSpan, start, end, 0);
        }
        return spannableStringBuilder;
    }

    @NonNull
    @Override
    public ImageView getImageView() {
        return imageView;
    }

    @NonNull
    @Override
    public String getImageName() {
        return card.getImageName();
    }

    public void startSearch(@NonNull final String title, @NonNull final CardRepository.Filter filter) {
        getFragmentManager().popBackStackImmediate();
        final Fragment target = getTargetFragment();
        if (target == null) return;
        final Intent resultIntent = new Intent();
        filter.setNormalOnly(PreferenceRepository.getHideNormal(getContext()));
        resultIntent.putExtra(RESULT_FILTER, filter);
        resultIntent.putExtra(RESULT_TITLE, title);
        target.onActivityResult(getTargetRequestCode(), RESULT_OK, resultIntent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return DeckRepository.newDecksLoader(getContext());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        final List<AbstractDeck> decks = DeckRepository.toDecks(data);
        if (decks.size() > 0)
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
                            DeckRepository.addCardIfNotExist(getContext(), decks.get(position).getId(), card.getSerial());
                        }
                    });
        else
            Snackbar.make(((SnackBarSupport) getActivity()).getCoordinatorLayout(), R.string.msg_no_deck, Snackbar.LENGTH_LONG)
                    .setAction(R.string.dialog_create_button, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DialogUtils.showCreateDeckDialog(getContext());
                        }
                    })
                    .show();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //no-ops
    }
}
