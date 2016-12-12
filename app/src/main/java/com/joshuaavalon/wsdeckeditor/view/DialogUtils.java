package com.joshuaavalon.wsdeckeditor.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.base.Joiner;
import com.google.common.collect.Multiset;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.config.Constant;
import com.joshuaavalon.wsdeckeditor.sdk.card.Card;
import com.joshuaavalon.wsdeckeditor.sdk.deck.Deck;
import com.joshuaavalon.wsdeckeditor.sdk.deck.DeckMeta;
import com.joshuaavalon.wsdeckeditor.sdk.deck.IDeckRepository;

import java.util.ArrayList;
import java.util.List;

public class DialogUtils {
    private static final String INFO_DIALOG_SEPARATOR = "\n";
    private static final String INFO_DIALOG_LINE_SEPARATOR = " / ";

    public static void showDeckSelectDialog(@NonNull final Context context, @NonNull final List<String> deckNames,
                                            final int selectedPosition,
                                            @NonNull final MaterialDialog.ListCallbackSingleChoice callback) {
        new MaterialDialog.Builder(context)
                .title(R.string.dialog_select_your_deck)
                .items(deckNames)
                .itemsCallbackSingleChoice(selectedPosition, callback)
                .positiveText(R.string.dialog_choose_button)
                .negativeText(R.string.dialog_cancel_button)
                .show();
    }

    public static void showCreateDeckDialog(@NonNull final Context context,
                                            @NonNull final IDeckRepository repository,
                                            @Nullable final CreateDeckCallback callback) {
        new MaterialDialog.Builder(context)
                .title(R.string.dialog_create_a_new_deck)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(R.string.dialog_deck_name, 0, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        final Deck deck = new Deck();
                        deck.setName(input.toString());
                        repository.save(deck);
                        if (callback != null)
                            callback.onCreate(deck);
                    }
                })
                .positiveText(R.string.dialog_create_button)
                .negativeText(R.string.dialog_cancel_button)
                .show();
    }

    public static void showDeckSelectDialog(@NonNull final Context context, @NonNull final List<String> deckNames,
                                            @NonNull final MaterialDialog.ListCallback callback) {
        new MaterialDialog.Builder(context)
                .title(R.string.dialog_add_to_deck)
                .items(deckNames)
                .itemsCallback(callback)
                .show();
    }

    public static void showDeckInfoDialog(@NonNull final Context context, @NonNull final Deck deck) {
        final MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title(R.string.dialog_deck_info)
                .customView(R.layout.dialog_deck_info, true)
                .positiveText(R.string.dialog_close_button)
                .show();
        final View view = dialog.getCustomView();
        if (view == null) return;
        final TextView expansionTextView = (TextView) view.findViewById(R.id.expansion_content_text_view);
        expansionTextView.setText(Joiner.on(INFO_DIALOG_SEPARATOR).join(deck.getExpansion()));
        final Multiset<Card.Color> colorCount = deck.getColor();
        final Multiset<Card.Type> typeCount = deck.getType();
        final Multiset<Integer> levelCount = deck.getLevel();
        final TextView totalTextView = (TextView) view.findViewById(R.id.total_content_text_view);
        totalTextView.setText(getStatusLabel(deck), TextView.BufferType.SPANNABLE);
        final List<String> tempList = new ArrayList<>();
        final TextView colorTextView = (TextView) view.findViewById(R.id.color_content_text_view);
        for (Card.Color color : Card.Color.values())
            tempList.add(String.valueOf(colorCount.count(color)));
        colorTextView.setText(Joiner.on(INFO_DIALOG_LINE_SEPARATOR).join(tempList));
        tempList.clear();
        final TextView typeTextView = (TextView) view.findViewById(R.id.type_content_text_view);
        for (Card.Type type : Card.Type.values())
            tempList.add(String.valueOf(typeCount.count(type)));
        typeTextView.setText(Joiner.on(INFO_DIALOG_LINE_SEPARATOR).join(tempList));
        tempList.clear();
        final TextView levelTextView = (TextView) view.findViewById(R.id.level_content_text_view);
        for (int i = 0; i <= 3; i++)
            tempList.add(String.valueOf(levelCount.count(i)));
        levelTextView.setText(Joiner.on(INFO_DIALOG_LINE_SEPARATOR).join(tempList));
    }

    public static void showRenameDeckDialog(@NonNull final Context context,
                                            @NonNull final DeckMeta meta,
                                            @NonNull final IDeckRepository deckRepository) {
        new MaterialDialog.Builder(context)
                .title(R.string.dialog_rename_deck)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(context.getString(R.string.dialog_deck_name), meta.getName(), false,
                        new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                deckRepository.save(new DeckMeta(meta.getId(), input.toString(), meta.getCover()));
                            }
                        })
                .positiveText(R.string.dialog_rename_button)
                .negativeText(R.string.dialog_cancel_button)
                .show();
    }

    private static CharSequence getStatusLabel(@NonNull final Deck deck) {
        final int count = deck.getCardList().size();
        final String countLabel = count > 99 ? "99+" : String.valueOf(count);
        final SpannableString countLabelSpan = new SpannableString(countLabel);
        if (count != Constant.DeckSize)
            countLabelSpan.setSpan(new ForegroundColorSpan(
                    (count > Constant.DeckSize) ? Color.RED : Color.GRAY), 0, countLabel.length(), 0);
        final SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(countLabelSpan);
        builder.append(" / ");
        builder.append(String.valueOf(Constant.DeckSize));
        return builder;
    }

    public interface CreateDeckCallback {
        void onCreate(@NonNull final Deck deck);
    }
}
