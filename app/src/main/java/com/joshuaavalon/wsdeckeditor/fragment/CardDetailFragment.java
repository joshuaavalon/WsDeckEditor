package com.joshuaavalon.wsdeckeditor.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.activity.MainActivity;
import com.joshuaavalon.wsdeckeditor.model.Card;
import com.joshuaavalon.wsdeckeditor.repository.CardRepository;
import com.joshuaavalon.wsdeckeditor.repository.model.CardFilter;
import com.joshuaavalon.wsdeckeditor.repository.model.KeywordCardFilterItem;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CardDetailFragment extends BaseFragment {
    private static final String CARD_SERIAL = "card_serial";
    private ImageView imageView;
    private TextView nameTextView;
    private TextView serialTextView;
    private TextView expansionTextView;
    private TextView rarityTextView;
    private ImageView sideImageView;
    private TextView typeTextView;
    private TextView colorTextView;
    private TextView levelTextView;
    private TextView costTextView;
    private TextView powerTextView;
    private TextView soulTextView;
    private TextView triggerTextView;
    private TextView attributeTextView;
    private TextView textTextView;
    private TextView flavorTextView;
    private Card card;

    @NonNull
    public static CardDetailFragment newInstance(@NonNull final String serial) {
        final CardDetailFragment fragment = new CardDetailFragment();
        final Bundle args = new Bundle();
        args.putString(CARD_SERIAL, serial);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!getArguments().containsKey(CARD_SERIAL)) return;
        final String serial = getArguments().getString(CARD_SERIAL);
        if (serial == null) return;
        final Optional<Card> cardOptional = CardRepository.getCardBySerial(serial);
        if (cardOptional.isPresent())
            card = cardOptional.get();
        else
            card = new Card.Builder().build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_card_detail, container, false);
        imageView = (ImageView) rootView.findViewById(R.id.card_detail_image);
        nameTextView = (TextView) rootView.findViewById(R.id.card_detail_name);
        serialTextView = (TextView) rootView.findViewById(R.id.card_detail_serial);
        expansionTextView = (TextView) rootView.findViewById(R.id.card_detail_expansion);
        rarityTextView = (TextView) rootView.findViewById(R.id.card_detail_rarity);
        sideImageView = (ImageView) rootView.findViewById(R.id.card_detail_side);
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
        bind(card);
        return rootView;
    }

    public void bind(final Card card) {
        imageView.setImageBitmap(CardRepository.getImage(card.getImage(), card.getType()));
        nameTextView.setText(card.getName());
        serialTextView.setText(card.getSerial());
        expansionTextView.setText(card.getExpansion());
        rarityTextView.setText(card.getRarity());
        sideImageView.setImageResource(card.getSide().getDrawable());
        typeTextView.setText(card.getType().getResId());
        colorTextView.setText(card.getColor().getResId());
        if (card.getType() != Card.Type.Climax) {
            levelTextView.setText(String.valueOf(card.getLevel()));
            costTextView.setText(String.valueOf(card.getCost()));
        } else {
            levelTextView.setText(R.string.not_applicable);
            costTextView.setText(R.string.not_applicable);
        }
        if (card.getType() == Card.Type.Character) {
            powerTextView.setText(String.valueOf(card.getPower()));
            soulTextView.setText(String.valueOf(card.getSoul()));
        } else {
            powerTextView.setText(R.string.not_applicable);
            soulTextView.setText(R.string.not_applicable);
        }
        triggerTextView.setText(card.getTrigger().getResId());
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

    private void startSearch(@NonNull final CardFilter cardFilter) {
        final Intent returnIntent = new Intent();
        returnIntent.putParcelableArrayListExtra(MainActivity.SEARCH_FILTER, cardFilter.getParcelableList());
        getActivity().setResult(Activity.RESULT_OK, returnIntent);
        getActivity().finish();
    }

    private ClickableSpan getCharaSpan(final String chara) {
        return new ClickableSpan() {
            @Override
            public void onClick(View view) {
                final CardFilter cardFilter = new CardFilter();
                cardFilter.addFilterItem(KeywordCardFilterItem.newCharInstance(chara));
                startSearch(cardFilter);
            }

            @Override
            public void updateDrawState(TextPaint textPaint) {
                super.updateDrawState(textPaint);
                textPaint.setUnderlineText(false);
            }
        };
    }

    private ClickableSpan getNameSpan(final String name) {
        return new ClickableSpan() {
            @Override
            public void onClick(View view) {
                final CardFilter cardFilter = new CardFilter();
                cardFilter.addFilterItem(KeywordCardFilterItem.newNameInstance(name));
                startSearch(cardFilter);
            }

            @Override
            public void updateDrawState(TextPaint textPaint) {
                super.updateDrawState(textPaint);
                textPaint.setUnderlineText(false);
            }
        };
    }

    private SpannableStringBuilder regexSpan(SpannableStringBuilder spannableStringBuilder,
                                             String regex,
                                             Function<String, ClickableSpan> factoryMethod) {
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
}
