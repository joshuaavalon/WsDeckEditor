package com.joshuaavalon.wsdeckeditor.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputType;

import com.afollestad.materialdialogs.MaterialDialog;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.sdk.deck.Deck;
import com.joshuaavalon.wsdeckeditor.sdk.deck.IDeckRepository;

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
                .positiveText(R.string.dialog_select_button)
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

    public interface CreateDeckCallback {
        void onCreate(@NonNull final Deck deck);
    }
}
