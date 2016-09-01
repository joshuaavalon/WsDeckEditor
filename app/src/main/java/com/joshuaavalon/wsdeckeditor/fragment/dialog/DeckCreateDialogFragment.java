package com.joshuaavalon.wsdeckeditor.fragment.dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.common.base.Strings;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.model.Deck;
import com.joshuaavalon.wsdeckeditor.repository.DeckRepository;

public class DeckCreateDialogFragment extends DialogFragment implements View.OnClickListener {

    private TextInputLayout textInputLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        getDialog().setTitle(R.string.create_a_new_deck);
        final View rootView = inflater.inflate(R.layout.dialog_deck_create, container, false);
        final Button cancelButton = (Button) rootView.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        final Button createButton = (Button) rootView.findViewById(R.id.create_button);
        createButton.setOnClickListener(this);
        textInputLayout = (TextInputLayout) rootView.findViewById(R.id.input_layout);
        return rootView;
    }

    @Override
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
        if (targetFragment instanceof DeckSelectDialogFragment) {
            ((DeckSelectDialogFragment) targetFragment).updateOptions();
        }
        dismiss();
    }

    public static void start(@NonNull final FragmentManager fragmentManager,
                             @NonNull final Fragment targetFragment) {
        final DeckCreateDialogFragment fragment = new DeckCreateDialogFragment();
        fragment.setTargetFragment(targetFragment, 0);
        fragment.show(fragmentManager, null);
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}
