package com.joshuaavalon.wsdeckeditor.fragment.dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.InputType;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.model.Card;
import com.joshuaavalon.wsdeckeditor.repository.CardRepository;

public class ChangeCardCountDialogFragment extends InputDialogFragment {
    private static final String ARG_SERIAL = "serial";
    private String serial;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        serial = args.getString(ARG_SERIAL);
    }

    @Override
    @NonNull
    protected String getTitle() {
        final Optional<Card> cardOptional = CardRepository.getCardBySerial(serial);
        if (cardOptional.isPresent())
            return cardOptional.get().getName();
        else
            return serial;
    }

    @Override
    @StringRes
    protected int getPositiveButtonResId() {
        return R.string.change_button;
    }

    @Override
    protected String getHint() {
        return getString(R.string.count);
    }

    @Override
    protected int getInputType() {
        return InputType.TYPE_CLASS_NUMBER;
    }

    @Override
    @NonNull
    protected Optional<String> getErrorMessage(@Nullable final String input) {
        if (Strings.isNullOrEmpty(input))
            return Optional.of(getString(R.string.card_count_error));
        else
            return Optional.absent();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void callback(final String input) {
        ((Callback) getTargetFragment()).changeCardCount(serial, Integer.valueOf(input));
    }

    public static <T extends Fragment & Callback>
    void start(@NonNull final FragmentManager fragmentManager,
               @NonNull final T targetFragment,
               @NonNull final String serial) {
        final DialogFragment fragment = new ChangeCardCountDialogFragment();
        fragment.setTargetFragment(targetFragment, 0);
        final Bundle args = new Bundle();
        args.putString(ARG_SERIAL, serial);
        fragment.setArguments(args);
        fragment.show(fragmentManager, null);
    }

    public interface Callback {
        void changeCardCount(@NonNull String serial, int count);
    }
}