package com.joshuaavalon.wsdeckeditor;


import android.content.Context;
import android.support.annotation.NonNull;
import android.text.InputType;

import com.afollestad.materialdialogs.MaterialDialog;
import com.joshuaavalon.wsdeckeditor.sdk.Deck;
import com.joshuaavalon.wsdeckeditor.sdk.data.DeckRepository;
import com.joshuaavalon.wsdeckeditor.sdk.util.AbstractDeck;

import java.util.List;

public class DialogUtils {
    public static void showCreateDeckDialog(@NonNull final Context context) {
        new MaterialDialog.Builder(context)
                .title(R.string.dialog_create_a_new_deck)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(R.string.dialog_deck_name, 0, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        final Deck deck = new Deck();
                        deck.setName(input.toString());
                        DeckRepository.createDeck(context, deck);
                    }
                }).show();
    }

    public static void showRenameDeckDialog(@NonNull final Context context, @NonNull final AbstractDeck absDeck) {
        new MaterialDialog.Builder(context)
                .title(R.string.dialog_create_a_new_deck)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(context.getString(R.string.dialog_deck_name), absDeck.getName(), false,
                        new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                DeckRepository.updateDeckName(context, absDeck.getId(), input.toString());
                            }
                        }).show();
    }

    public static void showDeckSelectDialog(@NonNull final Context context, @NonNull final List<String> deckNames,
                                            @NonNull final MaterialDialog.ListCallback callback) {
        new MaterialDialog.Builder(context)
                .title(R.string.dialog_select_your_deck)
                .items(deckNames)
                .itemsCallback(callback)
                .show();
    }
}
