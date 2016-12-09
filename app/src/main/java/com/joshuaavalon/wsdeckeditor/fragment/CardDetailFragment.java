package com.joshuaavalon.wsdeckeditor.fragment;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.joshuaavalon.android.view.ContentView;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.activity.ResultActivity;
import com.joshuaavalon.wsdeckeditor.sdk.card.Card;
import com.joshuaavalon.wsdeckeditor.sdk.card.Filter;
import com.joshuaavalon.wsdeckeditor.task.CardImageLoadTask;
import com.joshuaavalon.wsdeckeditor.view.CardImageHolder;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import timber.log.Timber;

@ContentView(R.layout.fragment_card_detail)
public class CardDetailFragment extends BaseFragment implements CardImageHolder {
    private static final String ARG_SERIAL = "CardDetailFragment.Serial";
    //region Views
    @BindView(R.id.card_detail_image)
    ImageView imageView;
    @BindView(R.id.card_detail_name)
    TextView nameTextView;
    @BindView(R.id.card_detail_serial)
    TextView serialTextView;
    @BindView(R.id.card_detail_expansion)
    TextView expansionTextView;
    @BindView(R.id.card_detail_rarity)
    TextView rarityTextView;
    @BindView(R.id.card_detail_side)
    TextView sideImageView;
    @BindView(R.id.card_detail_type)
    TextView typeTextView;
    @BindView(R.id.card_detail_color)
    TextView colorTextView;
    @BindView(R.id.card_detail_level)
    TextView levelTextView;
    @BindView(R.id.card_detail_cost)
    TextView costTextView;
    @BindView(R.id.card_detail_power)
    TextView powerTextView;
    @BindView(R.id.card_detail_soul)
    TextView soulTextView;
    @BindView(R.id.card_detail_trigger)
    TextView triggerTextView;
    @BindView(R.id.card_detail_attribute)
    TextView attributeTextView;
    @BindView(R.id.card_detail_text)
    TextView textTextView;
    @BindView(R.id.card_detail_flavor_text)
    TextView flavorTextView;
    //endregion
    private Card card;

    @NonNull
    public static CardDetailFragment newInstance(@NonNull final String serial) {
        final CardDetailFragment fragment = new CardDetailFragment();
        final Bundle args = new Bundle();
        args.putString(ARG_SERIAL, serial);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = super.onCreateView(inflater, container, savedInstanceState);
        final String serial = getArguments().getString(ARG_SERIAL);
        if (serial == null) {
            Timber.w("CardDetailFragment: Empty Argument");
            return rootView;
        }
        card = getCardRepository().find(serial);
        bind();
        return rootView;
    }

    //region Bind
    public void bind() {
        new CardImageLoadTask(getCardRepository(), this, card).execute();
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
        bindAttribute();
        bindText();
        flavorTextView.setText(card.getFlavor());
    }

    private void bindText() {
        final String cardText = card.getText();
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
                        return getCharaSpan(arg, textTextView);
                    }
                });
        textTextView.setText(spannableStringBuilder);
        textTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void bindAttribute() {
        final String first = card.getAttribute1();
        final String second = card.getAttribute2();
        if (TextUtils.isEmpty(first) && TextUtils.isEmpty(second)) {
            attributeTextView.setText(R.string.not_applicable_value);
            return;
        }
        SpannableString firstSpan = new SpannableString(first);
        firstSpan.setSpan(getCharaSpan(first, attributeTextView), 0, first.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        if (second.equals(""))
            attributeTextView.setText(firstSpan);
        else {
            SpannableString secondSpan = new SpannableString(second);
            secondSpan.setSpan(getCharaSpan(second, attributeTextView), 0, second.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            attributeTextView.setText(TextUtils.concat(firstSpan, "・", secondSpan));
        }
        attributeTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    //endregion
    private ClickableSpan getCharaSpan(@NonNull final String chara, @NonNull final View view) {
        return new ClickableSpan() {
            @Override
            public void onClick(View view) {
                final Filter cardFilter = new Filter();
                cardFilter.setHasName(false);
                cardFilter.setHasChara(true);
                cardFilter.setHasSerial(false);
                cardFilter.setHasText(false);
                final Set<String> keywords = new HashSet<>();
                keywords.add(chara);
                cardFilter.setKeyword(keywords);
                ResultActivity.start(getContext(), cardFilter, view);
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
                final Filter cardFilter = new Filter();
                cardFilter.setHasName(true);
                cardFilter.setHasChara(false);
                cardFilter.setHasSerial(false);
                cardFilter.setHasText(false);
                final Set<String> keywords = new HashSet<>();
                keywords.add(name);
                cardFilter.setKeyword(keywords);
                ResultActivity.start(getContext(), cardFilter, textTextView);
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
        return card.getImage();
    }
}
