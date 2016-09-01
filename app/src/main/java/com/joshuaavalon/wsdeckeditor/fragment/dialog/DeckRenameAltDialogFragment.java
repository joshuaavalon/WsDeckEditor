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

public class DeckRenameAltDialogFragment extends DialogFragment implements View.OnClickListener {
    private TextInputLayout textInputLayout;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setView(R.layout.dialog_deck_rename)
                .setTitle(R.string.rename_deck)
                .setPositiveButton(R.string.rename_button, null)
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
        final Fragment targetFragment = getTargetFragment();
        ((Handler<String>) targetFragment).handle(name);
        dismiss();
    }

    public static <T extends Fragment & Handler<?>>
    void start(@NonNull final FragmentManager fragmentManager,
               @NonNull final T targetFragment) {
        final DeckRenameAltDialogFragment fragment = new DeckRenameAltDialogFragment();
        fragment.setTargetFragment(targetFragment, 0);
        fragment.show(fragmentManager, null);
    }
}