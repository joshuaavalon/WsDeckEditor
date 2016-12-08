package com.joshuaavalon.wsdeckeditor.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import com.appyvet.rangebar.RangeBar;
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

@ContentView(R.layout.activity_search)
public class SearchActivity extends BaseActivity {
    private static final String ARG_FILTER = "SearchActivity.Filter";
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
    @BindView(R.id.level_range_bar)
    RangeBar levelRangeBar;
    @BindView(R.id.cost_range_bar)
    RangeBar costRangeBar;
    @BindView(R.id.power_range_bar)
    RangeBar powerRangeBar;
    @BindView(R.id.soul_range_bar)
    RangeBar soulRangeBar;
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

    public static void start(@NonNull final Context context, @Nullable Filter filter) {
        final Intent intent = new Intent(context, SearchActivity.class);
        final Bundle args = new Bundle();
        if (filter != null)
            args.putParcelable(ARG_FILTER, filter);
        intent.putExtras(args);
        context.startActivity(intent);
    }

    private static Filter.Range createRange(@NonNull final RangeBar rangeBar) {
        final Filter.Range range = new Filter.Range();
        final String minString = rangeBar.getLeftPinValue();
        if (!TextUtils.isEmpty(minString) && TextUtils.isDigitsOnly(minString))
            range.setMin(Integer.valueOf(minString));
        final String maxString = rangeBar.getRightPinValue();
        if (!TextUtils.isEmpty(maxString) && TextUtils.isDigitsOnly(maxString))
            range.setMax(Integer.valueOf(maxString));
        return range;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.nav_search);
        initializeTypeSpinner();
        initializeColorSpinner();
        initializeExpansionSpinner();
        initializeTriggerSpinner();
        normalSwitch.setChecked(getPreference().getHideNormal());
        if (getIntent().hasExtra(ARG_FILTER)) {
            final Filter filter = getIntent().getParcelableExtra(ARG_FILTER);
            initializeFilter(filter);
        }
    }

    @Override
    protected void initializeActionBar(@NonNull ActionBar actionBar) {
        super.initializeActionBar(actionBar);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
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
        setRange(filter.getLevel(), levelRangeBar);
        setRange(filter.getCost(), costRangeBar);
        final Filter.Range powerRange = filter.getPower();
        if (powerRange != null) {
            powerRange.setMax(powerRange.getMax() / 1000);
            powerRange.setMin(powerRange.getMin() / 1000);
        }
        setRange(powerRange, powerRangeBar);
        setRange(filter.getSoul(), soulRangeBar);
        normalSwitch.setChecked(filter.isNormalOnly());
    }

    private void setRange(@Nullable final Filter.Range range, @NonNull final RangeBar rangeBar) {
        if (range == null) return;
        int min = 0, max = 0;
        if (range.getMin() >= 0)
            min = range.getMin();
        if (range.getMax() >= 0)
            max = range.getMax();
        rangeBar.setRangePinsByValue(min, max);
    }

    private void initializeTypeSpinner() {
        final List<String> typeItems = new ArrayList<>();
        typeItems.add("");
        for (Card.Type type : Card.Type.values())
            typeItems.add(getString(type.getStringId()));
        typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, typeItems);
        typeSpinner.setAdapter(typeAdapter);
    }

    private void initializeColorSpinner() {
        final List<String> colorItems = new ArrayList<>();
        colorItems.add("");
        for (Card.Color color : Card.Color.values())
            colorItems.add(getString(color.getStringId()));
        colorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, colorItems);
        colorSpinner.setAdapter(colorAdapter);
    }

    private void initializeTriggerSpinner() {
        final List<String> triggerItems = new ArrayList<>();
        triggerItems.add("");
        for (Card.Trigger trigger : Card.Trigger.values())
            triggerItems.add(getString(trigger.getStringId()));
        triggerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, triggerItems);
        triggerSpinner.setAdapter(triggerAdapter);
    }

    private void initializeExpansionSpinner() {
        final List<String> items = new ArrayList<>();
        items.add("");
        items.addAll(getCardRepository().expansions());
        expansionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
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
        filter.setLevel(createRange(levelRangeBar));
        filter.setCost(createRange(costRangeBar));
        final Filter.Range powerRange = createRange(powerRangeBar);
        powerRange.setMax(powerRange.getMax() * 1000);
        powerRange.setMin(powerRange.getMin() * 1000);
        filter.setPower(powerRange);
        filter.setSoul(createRange(soulRangeBar));
        filter.setNormalOnly(normalSwitch.isChecked());
        ResultActivity.start(this, filter);
    }
}
