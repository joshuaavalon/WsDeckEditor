package com.joshuaavalon.wsdeckeditor.fragment.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

import com.google.common.base.Strings;
import com.joshuaavalon.wsdeckeditor.Handler;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.model.Deck;
import com.joshuaavalon.wsdeckeditor.repository.DeckRepository;

public class DeckCreateDialogFragment extends DialogFragment implements View.OnClickListener {
    private TextInputLayout textInputLayout;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setView(R.layout.dialog_deck_create)
                .setTitle(R.string.create_a_new_deck)
                .setPositiveButton(R.string.create_deck_create, null)
                .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dismiss();
                    }
                });
        final AlertDialog dialog = builder.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(this);
        textInputLayout = (TextInputLayout) dialog.findViewById(R.id.input_layout);
        return dialog;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onClick(View view) {
        final EditText editText = textInputLayout.getEditText();
        if (editText == null) return;
        final String name = editText.getText().toString();
        if (Strings.isNullOrEmpty(name)) {
            textInputLayout.setError(getString(R.string.deck_name_error));
            return;
        }
        final Deck deck = new Deck();
        deck.setName(name);
        DeckRepository.save(deck);
        final Fragment targetFragment = getTargetFragment();
        ((Handler<Void>) targetFragment).handle(null);
        dismiss();
    }

    public static <T extends Fragment & Handler<?>>
    void start(@NonNull final FragmentManager fragmentManager, @NonNull final T targetFragment) {
        final DeckCreateDialogFragment fragment = new DeckCreateDialogFragment();
        fragment.setTargetFragment(targetFragment, 0);
        fragment.show(fragmentManager, null);
    }
}
