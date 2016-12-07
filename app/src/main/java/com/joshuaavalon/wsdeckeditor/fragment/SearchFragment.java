package com.joshuaavalon.wsdeckeditor.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.joshuaavalon.android.view.ContentView;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.sdk.card.Card;
import com.joshuaavalon.wsdeckeditor.sdk.card.Filter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

@ContentView(R.layout.fragment_search)
public class SearchFragment extends BaseFragment {
    private static final String ARG_FILTER = "SearchFragment.Filter";
    //region Views
    @BindView(R.id.search_serial)
    Switch serialSwitch;
    @BindView(R.id.search_name)
    Switch nameSwitch;
    @BindView(R.id.search_attr)
    Switch attributeSwitch;
    @BindView(R.id.search_text)
    Switch textSwitch;
    @BindView(R.id.search_hide)
    Switch normalSwitch;
    @BindView(R.id.search_keyword)
    EditText keywordEditText;
    @BindView(R.id.search_min_level)
    EditText minLevelEditText;
    @BindView(R.id.search_max_level)
    EditText maxLevelEditText;
    @BindView(R.id.search_min_cost)
    EditText minCostEditText;
    @BindView(R.id.search_max_cost)
    EditText maxCostEditText;
    @BindView(R.id.search_min_power)
    EditText minPowerEditText;
    @BindView(R.id.search_max_power)
    EditText maxPowerEditText;
    @BindView(R.id.search_min_soul)
    EditText minSoulEditText;
    @BindView(R.id.search_max_soul)
    EditText maxSoulEditText;
    @BindView(R.id.search_expansion)
    Spinner expansionSpinner;
    @BindView(R.id.search_type)
    Spinner typeSpinner;
    @BindView(R.id.search_color)
    Spinner colorSpinner;
    @BindView(R.id.search_trigger)
    Spinner triggerSpinner;
    //endregion
    private ArrayAdapter<String> typeAdapter;
    private ArrayAdapter<String> colorAdapter;
    private ArrayAdapter<String> expansionAdapter;
    private ArrayAdapter<String> triggerAdapter;

    public static SearchFragment create(@Nullable final Filter filter) {
        final SearchFragment fragment = new SearchFragment();
        final Bundle args = new Bundle();
        if (filter != null)
            args.putParcelable(ARG_FILTER, filter);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        initializeTypeSpinner();
        initializeColorSpinner();
        initializeExpansionSpinner();
        initializeTriggerSpinner();
        normalSwitch.setChecked(getPreference().getHideNormal());
        if (getArguments().containsKey(ARG_FILTER)) {
            final Filter filter = getArguments().getParcelable(ARG_FILTER);
            if (filter != null)
                initializeFilter(filter);
        }
        return view;
    }

    private void initializeFilter(@NonNull final Filter filter) {
        keywordEditText.setText(Joiner.on(" ").join(filter.getKeyword()));
        serialSwitch.setChecked(filter.isHasSerial());
        nameSwitch.setChecked(filter.isHasName());
        attributeSwitch.setChecked(filter.isHasChara());
        textSwitch.setChecked(filter.isHasText());
        int position = expansionAdapter.getPosition(filter.getExpansion());
        if (position < 0)
            position = 0;
        expansionSpinner.setSelection(position);
        position = filter.getType() == null ? 0 : typeAdapter.getPosition(getString(filter.getType().getStringId()));
        if (position < 0)
            position = 0;
        typeSpinner.setSelection(position);
        position = filter.getColor() == null ? 0 : colorAdapter.getPosition(getString(filter.getColor().getStringId()));
        if (position < 0)
            position = 0;
        colorSpinner.setSelection(position);
        position = filter.getTrigger() == null ? 0 : triggerAdapter.getPosition(getString(filter.getTrigger().getStringId()));
        if (position < 0)
            position = 0;
        triggerSpinner.setSelection(position);
        setRange(filter.getLevel(), minLevelEditText, maxLevelEditText);
        setRange(filter.getCost(), minCostEditText, maxCostEditText);
        setRange(filter.getPower(), minPowerEditText, maxPowerEditText);
        setRange(filter.getSoul(), minSoulEditText, maxSoulEditText);
        normalSwitch.setChecked(filter.isNormalOnly());
    }

    private void setRange(@Nullable final Filter.Range range,
                          @NonNull final EditText minText,
                          @NonNull final EditText maxText) {
        if (range == null) return;
        if (range.getMin() >= 0)
            minText.setText(String.valueOf(range.getMin()));
        if (range.getMax() >= 0)
            maxText.setText(String.valueOf(range.getMax()));
    }

    private void initializeTypeSpinner() {
        final List<String> typeItems = new ArrayList<>();
        typeItems.add("");
        for (Card.Type type : Card.Type.values())
            typeItems.add(getString(type.getStringId()));
        typeAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, typeItems);
        typeSpinner.setAdapter(typeAdapter);
    }

    private void initializeColorSpinner() {
        final List<String> colorItems = new ArrayList<>();
        colorItems.add("");
        for (Card.Color color : Card.Color.values())
            colorItems.add(getString(color.getStringId()));
        colorAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, colorItems);
        colorSpinner.setAdapter(colorAdapter);
    }

    private void initializeTriggerSpinner() {
        final List<String> triggerItems = new ArrayList<>();
        triggerItems.add("");
        for (Card.Trigger trigger : Card.Trigger.values())
            triggerItems.add(getString(trigger.getStringId()));
        triggerAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, triggerItems);
        triggerSpinner.setAdapter(triggerAdapter);
    }

    private void initializeExpansionSpinner() {
        final List<String> items = new ArrayList<>();
        items.add("");
        items.addAll(getCardRepository().expansions());
        expansionAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, items);
        expansionSpinner.setAdapter(expansionAdapter);
    }

    @OnClick(R.id.fab)
    void search() {
        final Filter filter = new Filter();
        final String keyword = keywordEditText.getText().toString();
        if (!TextUtils.isEmpty(keyword))
            filter.setKeyword(Sets.newHashSet(keyword.split("\\s+")));
        filter.setHasSerial(serialSwitch.isChecked());
        filter.setHasName(nameSwitch.isChecked());
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
        filter.setNormalOnly(normalSwitch.isChecked());
        //((MainActivity) getActivity()).transactTo(CardListFragment.newInstance(filter), true);
    }

    private static Filter.Range createRange(@NonNull final EditText min, @NonNull final EditText max) {
        final Filter.Range range = new Filter.Range();
        final String minString = min.getText().toString();
        if (!TextUtils.isEmpty(minString) && TextUtils.isDigitsOnly(minString))
            range.setMin(Integer.valueOf(minString));
        final String maxString = max.getText().toString();
        if (!TextUtils.isEmpty(maxString) && TextUtils.isDigitsOnly(maxString))
            range.setMax(Integer.valueOf(maxString));
        return range;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getString(R.string.nav_search);
    }
}
