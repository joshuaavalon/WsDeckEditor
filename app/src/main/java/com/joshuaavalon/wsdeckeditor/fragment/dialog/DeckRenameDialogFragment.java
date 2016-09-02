package com.joshuaavalon.wsdeckeditor.fragment.dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.InputType;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.model.Deck;

public class DeckRenameDialogFragment extends InputDialogFragment {
    private static final String ARG_DECK_ID = "deckId";
    private static final String ARG_DECK_NAME = "name";
    private long deckId;
    private String name;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        deckId = args.getLong(ARG_DECK_ID);
        name = args.getString(ARG_DECK_NAME);
    }

    @NonNull
    @Override
    protected String getTitle() {
        return name;
    }

    @Override
    protected int getPositiveButtonResId() {
        return R.string.rename_button;
    }

    @Override
    protected String getHint() {
        return getString(R.string.deck_name);
    }

    @Override
    protected int getInputType() {
        return InputType.TYPE_CLASS_TEXT;
    }

    @NonNull
    @Override
    protected Optional<String> getErrorMessage(@Nullable String input) {
        if (Strings.isNullOrEmpty(input))
            return Optional.of(getString(R.string.deck_name_error));
        else
            return Optional.absent();
    }

    @Override
    protected void callback(String input) {
        ((Callback) getTargetFragment()).changeDeckName(deckId, input);
    }

    public static <T extends Fragment & Callback>
    void start(@NonNull final FragmentManager fragmentManager,
               @NonNull final T targetFragment,
               @NonNull final Deck deck) {
        final DeckRenameDialogFragment fragment = new DeckRenameDialogFragment();
        fragment.setTargetFragment(targetFragment, 0);
        final Bundle args = new Bundle();
        args.putLong(ARG_DECK_ID, deck.getId());
        args.putString(ARG_DECK_ID, deck.getName());
        fragment.setArguments(args);
        fragment.show(fragmentManager, null);
    }

    public interface Callback {
        void changeDeckName(long deckId, @NonNull String title);
    }
}