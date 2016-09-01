package com.joshuaavalon.wsdeckeditor.fragment.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.joshuaavalon.wsdeckeditor.Handler;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.model.Deck;
import com.joshuaavalon.wsdeckeditor.repository.DeckRepository;

import java.util.ArrayList;
import java.util.List;

public class DeckSelectDialogFragment extends DialogFragment implements View.OnClickListener,
        AdapterView.OnItemClickListener, Handler<Void> {
    private static final String ARG_SERIALS = "serials";
    private List<Deck> decks;
    private List<String> deckNames;
    private ArrayAdapter<String> adapter;
    private List<String> serials;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        serials = args.getStringArrayList(ARG_SERIALS);
        if (serials == null)
            serials = new ArrayList<>();
    }

    public static void start(@NonNull final FragmentManager fragmentManager,
                             @NonNull final List<String> serials) {
        final DialogFragment dialogFragment = new DeckSelectDialogFragment();
        final Bundle args = new Bundle();
        args.putStringArrayList(ARG_SERIALS, Lists.newArrayList(serials));
        dialogFragment.setArguments(args);
        dialogFragment.show(fragmentManager, null);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setView(R.layout.dialog_deck_select)
                .setTitle(R.string.select_your_deck);
        final AlertDialog dialog = builder.show();
        final ListView listView = (ListView) dialog.findViewById(R.id.list_view);
        deckNames = new ArrayList<>();
        updateLists();
        adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, deckNames);
        if (listView != null) {
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(this);
        }
        final Button createDeckButton = (Button) dialog.findViewById(R.id.create_deck_button);
        if (createDeckButton != null)
            createDeckButton.setOnClickListener(this);
        return dialog;
    }

    @Override
    public void onClick(View view) {
        DeckCreateDialogFragment.start(getChildFragmentManager(), this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        final Deck currentDeck = decks.get(position);
        for (String serial : serials)
            currentDeck.addIfNotExist(serial);
        DeckRepository.save(currentDeck);
        dismiss();
    }

    private void updateLists() {
        decks = DeckRepository.getDecks();
        deckNames.clear();
        Iterables.addAll(deckNames, Iterables.transform(decks, new Function<Deck, String>() {
            @Override
            public String apply(Deck input) {
                return input.getName();
            }
        }));
    }

    @Override
    public void handle(Void object) {
        updateLists();
        adapter.notifyDataSetChanged();
    }
}
