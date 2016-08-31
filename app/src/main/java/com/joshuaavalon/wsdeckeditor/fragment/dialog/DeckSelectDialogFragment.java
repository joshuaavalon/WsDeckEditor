package com.joshuaavalon.wsdeckeditor.fragment.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.model.Deck;
import com.joshuaavalon.wsdeckeditor.repository.DeckRepository;

import java.util.ArrayList;
import java.util.List;

public class DeckSelectDialogFragment extends DialogFragment implements View.OnClickListener,
        AdapterView.OnItemClickListener {
    private List<Deck> decks;
    private List<String> deckNames;
    private ArrayAdapter<String> adapter;
    private EditText editText;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        getDialog().setTitle(R.string.select_your_deck);
        final View rootView = inflater.inflate(R.layout.dialog_deck_select, container, false);
        final ListView listView = (ListView) rootView.findViewById(R.id.list_view);
        deckNames = new ArrayList<>();
        updateOptions();
        adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, deckNames);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        final Button createDeckButton = (Button) rootView.findViewById(R.id.create_deck_button);
        createDeckButton.setOnClickListener(this);
        return rootView;
    }

    public static void show(@NonNull final FragmentManager fragmentManager) {
        new DeckSelectDialogFragment().show(fragmentManager, null);
    }

    @Override
    public void onClick(View view) {
        DeckCreateDialogFragment.show(this, getChildFragmentManager());
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        DeckRepository.setCurrentDeck(decks.get(position));
    }

    public void updateOptions() {
        decks = DeckRepository.getDecks();
        deckNames.clear();
        Iterables.addAll(deckNames, Iterables.transform(decks, new Function<Deck, String>() {
            @Override
            public String apply(Deck input) {
                return input.getName();
            }
        }));
    }
}
