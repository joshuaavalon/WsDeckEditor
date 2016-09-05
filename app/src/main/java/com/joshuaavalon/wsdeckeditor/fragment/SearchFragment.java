package com.joshuaavalon.wsdeckeditor.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.base.Joiner;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.Utility;
import com.joshuaavalon.wsdeckeditor.model.Card;
import com.joshuaavalon.wsdeckeditor.repository.CardRepository;
import com.joshuaavalon.wsdeckeditor.repository.PreferenceRepository;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends BaseFragment {
    private static final String FILTER_KEY = "Filter_Key";
    private static final String SPLIT_REGEX = "\\s+";
    private static final int SEARCH_AREA_NAME = 0;
    private static final int SEARCH_AREA_SERIAL = 1;
    private static final int SEARCH_AREA_CHAR = 2;
    private static final int SEARCH_AREA_TEXT = 3;
    private TextInputEditText keywordAndTextView;
    private TextInputEditText keywordOrTextView;
    private TextInputEditText keywordNotTextView;
    private Switch normalSwitch;
    private Spinner expSpinner;
    private Spinner typeSpinner;
    private Spinner colorSpinner;
    private Spinner triggerSpinner;
    private TextInputEditText levelMaxTextView;
    private TextInputEditText levelMinTextView;
    private TextInputEditText costMaxTextView;
    private TextInputEditText costMinTextView;
    private TextInputEditText powerMaxTextView;
    private TextInputEditText powerMinTextView;
    private TextInputEditText soulMaxTextView;
    private TextInputEditText soulMinTextView;
    private TextView searchAreaTextView;
    private boolean[] searchAreaChecked;

    @Nullable
    public static SearchFragment newInstance(@Nullable final CardRepository.Filter filter) {
        final SearchFragment fragment = new SearchFragment();
        if (filter != null) {
            final Bundle args = new Bundle();
            args.putParcelable(SearchFragment.FILTER_KEY, filter);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        keywordAndTextView = (TextInputEditText) rootView.findViewById(R.id.search_and_text);
        keywordOrTextView = (TextInputEditText) rootView.findViewById(R.id.search_or_text);
        keywordNotTextView = (TextInputEditText) rootView.findViewById(R.id.search_not_text);
        normalSwitch = (Switch) rootView.findViewById(R.id.normal_only_switch);
        normalSwitch.setChecked(PreferenceRepository.getHideNormal());
        levelMaxTextView = (TextInputEditText) rootView.findViewById(R.id.search_level_max_text);
        levelMinTextView = (TextInputEditText) rootView.findViewById(R.id.search_level_min_text);
        costMaxTextView = (TextInputEditText) rootView.findViewById(R.id.search_cost_max_text);
        costMinTextView = (TextInputEditText) rootView.findViewById(R.id.search_cost_min_text);
        powerMaxTextView = (TextInputEditText) rootView.findViewById(R.id.search_power_max_text);
        powerMinTextView = (TextInputEditText) rootView.findViewById(R.id.search_power_min_text);
        soulMaxTextView = (TextInputEditText) rootView.findViewById(R.id.search_soul_max_text);
        soulMinTextView = (TextInputEditText) rootView.findViewById(R.id.search_soul_min_text);
        final LinearLayout searchAreaLinearLayout = (LinearLayout) rootView.findViewById(R.id.search_area_linear_layout);
        searchAreaLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSearchAreaDialog();
            }
        });
        searchAreaTextView = (TextView) rootView.findViewById(R.id.search_area_text_view);
        resetSearchArea();

        expSpinner = (Spinner) rootView.findViewById(R.id.search_side_spinner);
        ArrayList<String> expSpinnerItems = new ArrayList<>();
        expSpinnerItems.add("");
        expSpinnerItems.add(getString(R.string.side_w));
        expSpinnerItems.add(getString(R.string.side_s));
        expSpinnerItems.addAll(CardRepository.getExpansions());
        ArrayAdapter<String> expAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, expSpinnerItems);
        expSpinner.setAdapter(expAdapter);

        typeSpinner = (Spinner) rootView.findViewById(R.id.search_type_spinner);
        ArrayList<String> typeSpinnerItems = new ArrayList<>();
        typeSpinnerItems.add("");
        typeSpinnerItems.add(getString(R.string.type_chara));
        typeSpinnerItems.add(getString(R.string.type_event));
        typeSpinnerItems.add(getString(R.string.type_climax));
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, typeSpinnerItems);
        typeSpinner.setAdapter(typeAdapter);

        colorSpinner = (Spinner) rootView.findViewById(R.id.search_color_spinner);
        ArrayList<String> colorSpinnerItems = new ArrayList<>();
        colorSpinnerItems.add("");
        colorSpinnerItems.add(getString(R.string.color_yellow));
        colorSpinnerItems.add(getString(R.string.color_green));
        colorSpinnerItems.add(getString(R.string.color_red));
        colorSpinnerItems.add(getString(R.string.color_blue));
        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, colorSpinnerItems);
        colorSpinner.setAdapter(colorAdapter);

        triggerSpinner = (Spinner) rootView.findViewById(R.id.search_trigger_spinner);
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
        ArrayAdapter<String> triggerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, triggerSpinnerItems);
        triggerSpinner.setAdapter(triggerAdapter);

        final Button searchButton = (Button) rootView.findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit();
            }
        });


        final Button resetButton = (Button) rootView.findViewById(R.id.reset_button);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reset();
            }
        });

        if (getArguments() == null || !getArguments().containsKey(FILTER_KEY)) return rootView;
        final CardRepository.Filter filter = getArguments().getParcelable(FILTER_KEY);
        if (filter != null) {
            keywordAndTextView.setText(Utility.toString(filter.getAndList(), " "));
            keywordOrTextView.setText(Utility.toString(filter.getOrList(), " "));
            keywordNotTextView.setText(Utility.toString(filter.getNotList(), " "));
            resetSearchArea(filter.isEnableName(),
                    filter.isEnableSerial(),
                    filter.isEnableChara(),
                    filter.isEnableText());
        }
        return rootView;
    }

    private void submit() {
        final CardRepository.Filter filter = new CardRepository.Filter();
        final String andKeywords = keywordAndTextView.getText().toString();
        if (!andKeywords.isEmpty())
            for (String keyword : andKeywords.split(SPLIT_REGEX))
                filter.addAnd(keyword);

        final String orKeywords = keywordOrTextView.getText().toString();
        if (!orKeywords.isEmpty())
            for (String keyword : orKeywords.split(SPLIT_REGEX))
                filter.addOr(keyword);

        final String notKeywords = keywordNotTextView.getText().toString();
        if (!notKeywords.isEmpty())
            for (String keyword : notKeywords.split(SPLIT_REGEX))
                filter.addNot(keyword);

        filter.setEnableName(searchAreaChecked[SEARCH_AREA_NAME]);
        filter.setEnableSerial(searchAreaChecked[SEARCH_AREA_SERIAL]);
        filter.setEnableChara(searchAreaChecked[SEARCH_AREA_CHAR]);
        filter.setEnableText(searchAreaChecked[SEARCH_AREA_TEXT]);
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
            filter.setMinPower(Integer.valueOf(powerMinTextView.getText().toString()));
        }
        if (!soulMaxTextView.getText().toString().equals("")) {
            filter.setMaxSoul(Integer.valueOf(soulMaxTextView.getText().toString()));
        }
        if (!soulMinTextView.getText().toString().equals("")) {
            filter.setMinSoul(Integer.valueOf(soulMinTextView.getText().toString()));
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
        resetSearchArea();
    }

    private void showSearchAreaDialog() {
        final MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                .title(R.string.search_area)
                .items(R.array.search_area)
                .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMultiChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                        for (int i = 0; i < searchAreaChecked.length; i++)
                            searchAreaChecked[i] = false;
                        for (int position : which)
                            searchAreaChecked[position] = true;
                        return true;
                    }
                })
                .positiveText(R.string.confirm_button)
                .show();
        final List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < searchAreaChecked.length; i++)
            if (searchAreaChecked[i])
                indices.add(i);
        dialog.setSelectedIndices(indices.toArray(new Integer[indices.size()]));
    }

    private void setSearchAreaTextView() {
        final List<String> searchAreaList = new ArrayList<>();
        final String[] searchAreaMessage = getResources().getStringArray(R.array.search_area);
        for (int i = 0; i < searchAreaChecked.length; i++)
            if (searchAreaChecked[i])
                searchAreaList.add(searchAreaMessage[i]);
        searchAreaTextView.setText(Joiner.on(", ").join(searchAreaList));
    }

    private void resetSearchArea() {
        resetSearchArea(true, true, true, true);
    }

    private void resetSearchArea(boolean name, boolean serial, boolean chara, boolean text) {
        searchAreaChecked = new boolean[]{name, serial, chara, text};
        setSearchAreaTextView();
    }
}