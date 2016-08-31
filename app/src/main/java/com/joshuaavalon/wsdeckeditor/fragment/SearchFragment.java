package com.joshuaavalon.wsdeckeditor.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;

import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.Utility;
import com.joshuaavalon.wsdeckeditor.model.Card;
import com.joshuaavalon.wsdeckeditor.repository.CardRepository;

import java.util.ArrayList;

public class SearchFragment extends BaseFragment {
    public static final String FILTER_KEY = "Filter_Key";
    private static final String SPLIT_REGEX = "\\s+";
    private EditText keywordAndTextView;
    private EditText keywordOrTextView;
    private EditText keywordNotTextView;
    private Switch nameSwitch;
    private Switch serialSwitch;
    private Switch charaSwitch;
    private Switch textSwitch;
    private Switch normalSwitch;
    private Spinner expSpinner;
    private Spinner typeSpinner;
    private Spinner colorSpinner;
    private Spinner triggerSpinner;
    private EditText levelMaxTextView;
    private EditText levelMinTextView;
    private EditText costMaxTextView;
    private EditText costMinTextView;
    private EditText powerMaxTextView;
    private EditText powerMinTextView;
    private EditText soulMaxTextView;
    private EditText soulMinTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_search, container, false);
        keywordAndTextView = (EditText) linearLayout.findViewById(R.id.search_and_text);
        keywordOrTextView = (EditText) linearLayout.findViewById(R.id.search_or_text);
        keywordNotTextView = (EditText) linearLayout.findViewById(R.id.search_not_text);
        nameSwitch = (Switch) linearLayout.findViewById(R.id.search_name_switch);
        serialSwitch = (Switch) linearLayout.findViewById(R.id.search_serial_switch);
        charaSwitch = (Switch) linearLayout.findViewById(R.id.search_char_switch);
        textSwitch = (Switch) linearLayout.findViewById(R.id.search_text_switch);
        normalSwitch = (Switch) linearLayout.findViewById(R.id.normal_only_switch);
        levelMaxTextView = (EditText) linearLayout.findViewById(R.id.search_level_max_text);
        levelMinTextView = (EditText) linearLayout.findViewById(R.id.search_level_min_text);
        costMaxTextView = (EditText) linearLayout.findViewById(R.id.search_cost_max_text);
        costMinTextView = (EditText) linearLayout.findViewById(R.id.search_cost_min_text);
        powerMaxTextView = (EditText) linearLayout.findViewById(R.id.search_power_max_text);
        powerMinTextView = (EditText) linearLayout.findViewById(R.id.search_power_min_text);
        soulMaxTextView = (EditText) linearLayout.findViewById(R.id.search_soul_max_text);
        soulMinTextView = (EditText) linearLayout.findViewById(R.id.search_soul_min_text);

        expSpinner = (Spinner) linearLayout.findViewById(R.id.search_side_spinner);
        ArrayList<String> expSpinnerItems = new ArrayList<>();
        expSpinnerItems.add("");
        expSpinnerItems.add(getString(R.string.side_w));
        expSpinnerItems.add(getString(R.string.side_w));
        expSpinnerItems.addAll(CardRepository.getExpansions());
        ArrayAdapter<String> expAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, expSpinnerItems);
        expSpinner.setAdapter(expAdapter);

        typeSpinner = (Spinner) linearLayout.findViewById(R.id.search_type_spinner);
        ArrayList<String> typeSpinnerItems = new ArrayList<>();
        typeSpinnerItems.add("");
        typeSpinnerItems.add(getString(R.string.type_chara));
        typeSpinnerItems.add(getString(R.string.type_event));
        typeSpinnerItems.add(getString(R.string.type_climax));
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, typeSpinnerItems);
        typeSpinner.setAdapter(typeAdapter);

        colorSpinner = (Spinner) linearLayout.findViewById(R.id.search_color_spinner);
        ArrayList<String> colorSpinnerItems = new ArrayList<>();
        colorSpinnerItems.add("");
        colorSpinnerItems.add(getString(R.string.color_yellow));
        colorSpinnerItems.add(getString(R.string.color_green));
        colorSpinnerItems.add(getString(R.string.color_red));
        colorSpinnerItems.add(getString(R.string.color_blue));
        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, colorSpinnerItems);
        colorSpinner.setAdapter(colorAdapter);

        triggerSpinner = (Spinner) linearLayout.findViewById(R.id.search_trigger_spinner);
        ArrayList<String> triggerSpinnerItems = new ArrayList<>();
        triggerSpinnerItems.add("");
        triggerSpinnerItems.add(getString(R.string.trigger_none));
        triggerSpinnerItems.add(getString(R.string.trigger_1s));
        triggerSpinnerItems.add(getString(R.string.trigger_2s));
        triggerSpinnerItems.add(getString(R.string.trigger_wind));
        triggerSpinnerItems.add(getString(R.string.trigger_fire));
        triggerSpinnerItems.add(getString(R.string.trigger_bag));
        triggerSpinnerItems.add(getString(R.string.trigger_gold));
        triggerSpinnerItems.add(getString(R.string.trigger_door));
        triggerSpinnerItems.add(getString(R.string.trigger_book));
        triggerSpinnerItems.add(getString(R.string.trigger_gate));
        ArrayAdapter<String> triggerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, triggerSpinnerItems);
        triggerSpinner.setAdapter(triggerAdapter);

        Button searchButton = (Button) linearLayout.findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit();
            }
        });


        Button resetButton = (Button) linearLayout.findViewById(R.id.reset_button);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reset();
            }
        });

        if (getArguments() == null || !getArguments().containsKey(FILTER_KEY)) return linearLayout;
        CardRepository.Filter filter = getArguments().getParcelable(FILTER_KEY);
        if (filter != null) {
            keywordAndTextView.setText(Utility.toString(filter.getAndList(), " "));
            keywordOrTextView.setText(Utility.toString(filter.getOrList(), " "));
            keywordNotTextView.setText(Utility.toString(filter.getNotList(), " "));
            nameSwitch.setChecked(filter.isEnableName());
            serialSwitch.setChecked(filter.isEnableSerial());
            charaSwitch.setChecked(filter.isEnableChara());
            textSwitch.setChecked(filter.isEnableText());
        }
        return linearLayout;
    }

    private void submit() {
        final CardRepository.Filter filter = new CardRepository.Filter();
        final String andKeywords = keywordAndTextView.getText().toString();
        if (!andKeywords.isEmpty())
            for (String keyword : andKeywords.split(SPLIT_REGEX))
                filter.addAnd(keyword);

        String orKeywords = keywordOrTextView.getText().toString();
        if (!orKeywords.isEmpty())
            for (String keyword : orKeywords.split(SPLIT_REGEX))
                filter.addOr(keyword);

        String notKeywords = keywordNotTextView.getText().toString();
        if (!notKeywords.isEmpty())
            for (String keyword : notKeywords.split(SPLIT_REGEX))
                filter.addNot(keyword);

        filter.setEnableName(nameSwitch.isChecked());
        filter.setEnableSerial(serialSwitch.isChecked());
        filter.setEnableChara(charaSwitch.isChecked());
        filter.setEnableText(textSwitch.isChecked());
        filter.setNormalOnly(normalSwitch.isChecked());

        switch (expSpinner.getSelectedItemPosition()) {
            case 0:
                break;
            case 1:
                filter.setSide(Card.Side.W.toString());
                break;
            case 2:
                filter.setSide(Card.Side.S.toString());
                break;
            default:
                filter.setExpansion(expSpinner.getSelectedItem().toString());
        }

        switch (typeSpinner.getSelectedItemPosition()) {
            case 1:
                filter.setType(Card.Type.Character.toString());
                break;
            case 2:
                filter.setType(Card.Type.Event.toString());
                break;
            case 3:
                filter.setType(Card.Type.Climax.toString());
                break;
        }

        switch (colorSpinner.getSelectedItemPosition()) {
            case 1:
                filter.setColor(Card.Color.Yellow.toString());
                break;
            case 2:
                filter.setColor(Card.Color.Green.toString());
                break;
            case 3:
                filter.setColor(Card.Color.Red.toString());
                break;
            case 4:
                filter.setColor(Card.Color.Blue.toString());
                break;
        }

        switch (triggerSpinner.getSelectedItemPosition()) {
            case 1:
                filter.setTrigger(Card.Trigger.None.toString());
                break;
            case 2:
                filter.setTrigger(Card.Trigger.OneSoul.toString());
                break;
            case 3:
                filter.setTrigger(Card.Trigger.TwoSoul.toString());
                break;
            case 4:
                filter.setTrigger(Card.Trigger.Wind.toString());
                break;
            case 5:
                filter.setTrigger(Card.Trigger.Fire.toString());
                break;
            case 6:
                filter.setTrigger(Card.Trigger.Bag.toString());
                break;
            case 7:
                filter.setTrigger(Card.Trigger.Gold.toString());
                break;
            case 8:
                filter.setTrigger(Card.Trigger.Door.toString());
                break;
            case 9:
                filter.setTrigger(Card.Trigger.Book.toString());
                break;
            case 10:
                filter.setTrigger(Card.Trigger.Gate.toString());
                break;
        }

        if (!levelMaxTextView.getText().toString().equals("")) {
            filter.setMaxLevel(Integer.valueOf(levelMaxTextView.getText().toString()));
        }
        if (!levelMinTextView.getText().toString().equals("")) {
            filter.setMinLevel(Integer.valueOf(levelMinTextView.getText().toString()));
        }
        if (!costMaxTextView.getText().toString().equals("")) {
            filter.setMaxCost(Integer.valueOf(costMaxTextView.getText().toString()));
        }
        if (!costMinTextView.getText().toString().equals("")) {
            filter.setMinCost(Integer.valueOf(costMinTextView.getText().toString()));
        }
        if (!powerMaxTextView.getText().toString().equals("")) {
            filter.setMaxPower(Integer.valueOf(powerMaxTextView.getText().toString()));
        }
        if (!powerMinTextView.getText().toString().equals("")) {
            filter.setMinPower(Integer.valueOf(levelMaxTextView.getText().toString()));
        }
        if (!soulMaxTextView.getText().toString().equals("")) {
            filter.setMaxSoul(Integer.valueOf(levelMaxTextView.getText().toString()));
        }
        if (!soulMinTextView.getText().toString().equals("")) {
            filter.setMinSoul(Integer.valueOf(levelMaxTextView.getText().toString()));
        }

        final CardListFragment fragment = CardListFragment.newInstance(filter);
        getFragmentManager().beginTransaction()
                .replace(R.id.frame_content, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void reset() {
        keywordAndTextView.setText("");
        keywordOrTextView.setText("");
        keywordNotTextView.setText("");
        nameSwitch.setChecked(true);
        serialSwitch.setChecked(true);
        charaSwitch.setChecked(true);
        textSwitch.setChecked(true);
        levelMaxTextView.setText("");
        levelMinTextView.setText("");
        costMaxTextView.setText("");
        costMinTextView.setText("");
        powerMaxTextView.setText("");
        powerMinTextView.setText("");
        soulMaxTextView.setText("");
        soulMinTextView.setText("");
        expSpinner.setSelection(0);
        typeSpinner.setSelection(0);
        colorSpinner.setSelection(0);
        triggerSpinner.setSelection(0);
    }
}