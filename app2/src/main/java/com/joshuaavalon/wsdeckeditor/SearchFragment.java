package com.joshuaavalon.wsdeckeditor;


import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import com.google.common.collect.Sets;
import com.joshuaavalon.wsdeckeditor.sdk.Card;
import com.joshuaavalon.wsdeckeditor.sdk.data.CardRepository;
import com.joshuaavalon.wsdeckeditor.sdk.util.Range;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor>,
        View.OnClickListener {
    private Switch serialSwitch, nameSwtich, attributeSwitch, textSwitch;
    private EditText keywordEditText, minLevelEditText, maxLevelEditText, minCostEditText,
            maxCostEditText, minPowerEditText, maxPowerEditText, minSoulEditText, maxSoulEditText;
    private Spinner expansionSpinner, typeSpinner, colorSpinner, triggerSpinner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_search, container, false);
        keywordEditText = (EditText) view.findViewById(R.id.search_keyword);
        minLevelEditText = (EditText) view.findViewById(R.id.search_min_level);
        maxLevelEditText = (EditText) view.findViewById(R.id.search_max_level);
        minCostEditText = (EditText) view.findViewById(R.id.search_min_cost);
        maxCostEditText = (EditText) view.findViewById(R.id.search_max_cost);
        minPowerEditText = (EditText) view.findViewById(R.id.search_min_power);
        maxPowerEditText = (EditText) view.findViewById(R.id.search_max_power);
        minSoulEditText = (EditText) view.findViewById(R.id.search_min_soul);
        maxSoulEditText = (EditText) view.findViewById(R.id.search_max_soul);
        serialSwitch = (Switch) view.findViewById(R.id.search_serial);
        nameSwtich = (Switch) view.findViewById(R.id.search_name);
        attributeSwitch = (Switch) view.findViewById(R.id.search_attr);
        textSwitch = (Switch) view.findViewById(R.id.search_text);
        expansionSpinner = (Spinner) view.findViewById(R.id.search_expansion);

        typeSpinner = (Spinner) view.findViewById(R.id.search_type);
        final List<String> typeItems = new ArrayList<>();
        typeItems.add("");
        for (Card.Type type : Card.Type.values())
            typeItems.add(getString(type.getStringId()));
        typeSpinner.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, typeItems));

        colorSpinner = (Spinner) view.findViewById(R.id.search_color);
        final List<String> colorItems = new ArrayList<>();
        colorItems.add("");
        for (Card.Color color : Card.Color.values())
            colorItems.add(getString(color.getStringId()));
        colorSpinner.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, colorItems));

        triggerSpinner = (Spinner) view.findViewById(R.id.search_trigger);
        final List<String> triggerItems = new ArrayList<>();
        triggerItems.add("");
        for (Card.Trigger trigger : Card.Trigger.values())
            triggerItems.add(getString(trigger.getStringId()));
        triggerSpinner.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, triggerItems));

        getActivity().getSupportLoaderManager().initLoader(LoaderId.ExpansionLoader, null, this);
        final Button searchButton = (Button) view.findViewById(R.id.search_button);
        searchButton.setOnClickListener(this);
        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return CardRepository.newExpansionLoader(getContext());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        final List<String> items = new ArrayList<>();
        items.add("");
        items.addAll(CardRepository.toExpansions(data));
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, items);
        expansionSpinner.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //no-ops
    }

    @Override
    public void onClick(View v) {
        final CardRepository.Filter filter = new CardRepository.Filter();
        final String keyword = keywordEditText.getText().toString();
        if (!TextUtils.isEmpty(keyword))
            filter.setKeyword(Sets.newHashSet(keyword.split("\\s+")));
        filter.setHasSerial(serialSwitch.isChecked());
        filter.setHasName(nameSwtich.isChecked());
        filter.setHasChara(attributeSwitch.isChecked());
        filter.setHasText(textSwitch.isChecked());
        if (expansionSpinner.getSelectedItemPosition() != 0)
            filter.setExpansion((String) expansionSpinner.getSelectedItem());
        if (typeSpinner.getSelectedItemPosition() != 0)
            filter.setType(Card.Type.values()[typeSpinner.getSelectedItemPosition() - 1]);
        if (colorSpinner.getSelectedItemPosition() != 0)
            filter.setColor(Card.Color.values()[colorSpinner.getSelectedItemPosition() - 1]);
        if (triggerSpinner.getSelectedItemPosition() != 0)
            filter.setTrigger(Card.Trigger.values()[triggerSpinner.getSelectedItemPosition() - 1]);
        filter.setLevel(createRange(minLevelEditText, maxLevelEditText));
        filter.setCost(createRange(minCostEditText, maxCostEditText));
        filter.setPower(createRange(minPowerEditText, maxPowerEditText));
        filter.setSoul(createRange(minSoulEditText, maxSoulEditText));
        ((MainActivity) getActivity()).transactTo(CardListFragment.newInstance(filter), true);
    }

    private static Range createRange(@NonNull final EditText min, @NonNull final EditText max) {
        final Range range = new Range();
        final String minString = min.getText().toString();
        if (!TextUtils.isEmpty(minString) && TextUtils.isDigitsOnly(minString))
            range.setMin(Integer.valueOf(minString));
        final String maxString = max.getText().toString();
        if (!TextUtils.isEmpty(maxString) && TextUtils.isDigitsOnly(maxString))
            range.setMax(Integer.valueOf(maxString));
        return range;
    }
}
