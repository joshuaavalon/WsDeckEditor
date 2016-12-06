package com.joshuaavalon.wsdeckeditor.exclude;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.base.Joiner;
import com.google.common.collect.Multiset;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.sdk.card.Card;
import com.joshuaavalon.wsdeckeditor.sdk.deck.Deck;
import com.joshuaavalon.wsdeckeditor.sdk.util.DeckUtils;

import java.util.ArrayList;
import java.util.List;

public class DialogUtils {
    private static final String INFO_DIALOG_SEPARATOR = "\n";
    private static final String INFO_DIALOG_LINE_SEPARATOR = " / ";

    public static void showCreateDeckDialog(@NonNull final Context context) {
        showCreateDeckDialog(context, null);
    }

    public static void showCreateDeckDialog(@NonNull final Context context, @Nullable final CreateDeckCallback callback) {
        new MaterialDialog.Builder(context)
                .title(R.string.dialog_create_a_new_deck)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(R.string.dialog_deck_name, 0, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        final Deck deck = new Deck();
                        deck.setName(input.toString());
                        DeckRepository.createDeck(context, deck);
                        if (callback != null)
                            callback.onCreate(deck);
                    }
                }).show();
    }

    public static void showRenameDeckDialog(@NonNull final Context context, @NonNull final AbstractDeck absDeck) {
        new MaterialDialog.Builder(context)
                .title(R.string.dialog_rename_deck)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(context.getString(R.string.dialog_deck_name), absDeck.getName(), false,
                        new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                DeckRepository.updateDeck(context,
                                        new AbstractDeck(absDeck.getId(), input.toString(), absDeck.getCover()));
                            }
                        }).show();
    }

    public static void showDeckSelectDialog(@NonNull final Context context, @NonNull final List<String> deckNames,
                                            final int selectedPosition,
                                            @NonNull final MaterialDialog.ListCallbackSingleChoice callback) {
        new MaterialDialog.Builder(context)
                .title(R.string.dialog_select_your_deck)
                .items(deckNames)
                .itemsCallbackSingleChoice(selectedPosition, callback)
                .positiveText(R.string.dialog_select_button)
                .show();
    }

    public static void showDeckSelectDialog(@NonNull final Context context, @NonNull final List<String> deckNames,
                                            @NonNull final MaterialDialog.ListCallback callback) {
        new MaterialDialog.Builder(context)
                .title(R.string.dialog_select_your_deck)
                .items(deckNames)
                .itemsCallback(callback)
                .show();
    }

    public static void showDeckInfoDialog(@NonNull final Context context, @NonNull final Deck deck) {
        final MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title(R.string.dialog_deck_info)
                .customView(R.layout.dialog_deck_info, true)
                .show();
        final View view = dialog.getCustomView();
        if (view == null) return;
        final TextView expansionTextView = (TextView) view.findViewById(R.id.expansion_content_text_view);
        expansionTextView.setText(Joiner.on(INFO_DIALOG_SEPARATOR).join(deck.getExpansion()));
        final Multiset<Card.Color> colorCount = deck.getColor();
        final Multiset<Card.Type> typeCount = deck.getType();
        final Multiset<Integer> levelCount = deck.getLevel();
        final TextView totalTextView = (TextView) view.findViewById(R.id.total_content_text_view);
        totalTextView.setText(DeckUtils.getStatusLabel(deck), TextView.BufferType.SPANNABLE);
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

    public interface CreateDeckCallback {
        void onCreate(@NonNull final Deck deck);
    }
}
