package com.joshuaavalon.wsdeckeditor.fragment.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

import com.google.common.base.Optional;
import com.joshuaavalon.wsdeckeditor.R;

public abstract class InputDialogFragment extends DialogFragment implements View.OnClickListener {
    private TextInputLayout textInputLayout;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setView(R.layout.dialog_input)
                .setTitle(getTitle())
                .setPositiveButton(getPositiveButtonResId(), null)
                .setNegativeButton(getNegativeButtonResId(), getNegativeButtonListener());
        final AlertDialog dialog = builder.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(this);
        textInputLayout = (TextInputLayout) dialog.findViewById(R.id.input_layout);
        final TextInputEditText textInputEditText = (TextInputEditText) dialog.findViewById(R.id.edit_text);
        if (textInputEditText != null) {
            textInputEditText.setHint(getHint());
            textInputEditText.setInputType(getInputType());
        }
        return dialog;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onClick(View view) {
        final EditText editText = textInputLayout.getEditText();
        if (editText == null) return;
        final String input = editText.getText().toString();
        final Optional<String> errorMessageOptional = getErrorMessage(input);
        if (errorMessageOptional.isPresent()) {
            textInputLayout.setError(errorMessageOptional.get());
            return;
        }
        callback(input);
        dismiss();
    }

    @NonNull
    protected abstract String getTitle();

    @StringRes
    protected abstract int getPositiveButtonResId();

    @StringRes
    protected abstract String getHint();

    protected abstract int getInputType();

    @StringRes
    protected int getNegativeButtonResId() {
        return R.string.cancel_button;
    }

    @Nullable
    protected DialogInterface.OnClickListener getNegativeButtonListener() {
        return null;
    }

    @NonNull
    protected abstract Optional<String> getErrorMessage(@Nullable String input);

    protected abstract void callback(String input);
}
