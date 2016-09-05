package com.joshuaavalon.wsdeckeditor.repository.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputEditText;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.joshuaavalon.fluentquery.Condition;
import com.joshuaavalon.wsdeckeditor.Handler;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.repository.CardRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KeywordCardFilterItem extends CardFilterItem {
    private static final int SEARCH_AREA_NAME = 0;
    private static final int SEARCH_AREA_SERIAL = 1;
    private static final int SEARCH_AREA_CHAR = 2;
    private static final int SEARCH_AREA_TEXT = 3;
    private static final String SPLIT_REGEX = "\\s+";
    private final Set<String> phases;
    private final boolean isNot;
    private TextView searchAreaTextView;
    private final boolean[] searchAreaChecked;

    public KeywordCardFilterItem(final boolean isNot) {
        this.isNot = isNot;
        phases = new HashSet<>();
        searchAreaChecked = new boolean[]{true, true, true, true};
    }

    @NonNull
    @Override
    public Optional<Condition> toCondition() {
        final Condition condition = getCondition();
        if (!isNot)
            return Optional.fromNullable(condition);
        return condition == null ? Optional.<Condition>absent() : Optional.of(condition.not());
    }

    @NonNull
    @Override
    public MaterialDialog getDialog(@NonNull final Context context,
                                    @NonNull final Handler<Void> callback) {
        final MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title(getTitle())
                .customView(R.layout.dialog_keyword, false)
                .positiveText(R.string.confirm_button)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        setPhases(searchAreaTextView.getText().toString());
                        callback.handle(null);
                    }
                })
                .negativeText(R.string.cancel_button)
                .show();
        final View view = dialog.getCustomView();
        if (view != null) {
            final LinearLayout searchAreaLinearLayout = (LinearLayout) view.findViewById(R.id.search_area_linear_layout);
            searchAreaLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showSearchAreaDialog(context);
                }
            });
            final TextInputEditText editText = (TextInputEditText) view.findViewById(R.id.edit_text);
            editText.setHint(getTitle());
            searchAreaTextView = (TextView) view.findViewById(R.id.search_area_text_view);
        }
        return dialog;
    }

    @Override
    @StringRes
    public int getTitle() {
        return isNot ? R.string.not : R.string.and;
    }

    public void setPhases(@NonNull final String phases) {
        this.phases.clear();
        for (String phase : phases.split(SPLIT_REGEX))
            if (!Strings.isNullOrEmpty(phase))
                this.phases.add(phase);
    }

    private void setSearchAreaTextView(@NonNull final Context context) {
        final List<String> searchAreaList = new ArrayList<>();
        final String[] searchAreaMessage = context.getResources().getStringArray(R.array.search_area);
        for (int i = 0; i < searchAreaChecked.length; i++)
            if (searchAreaChecked[i])
                searchAreaList.add(searchAreaMessage[i]);
        searchAreaTextView.setText(Joiner.on(", ").join(searchAreaList));
    }

    private void showSearchAreaDialog(@NonNull final Context context) {
        final MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title(R.string.search_area)
                .iconRes(R.drawable.ic_search_black_24dp)
                .items(R.array.search_area)
                .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMultiChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                        for (int i = 0; i < searchAreaChecked.length; i++)
                            searchAreaChecked[i] = false;
                        for (int position : which)
                            searchAreaChecked[position] = true;
                        setSearchAreaTextView(context);
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

    @NonNull
    @Override
    public String getContent() {
        return Joiner.on(", ").join(phases);
    }

    @Nullable
    private Condition getCondition() {
        final List<Condition> conditions = new ArrayList<>();
        final List<Condition> subConditions = new ArrayList<>();
        for (String phase : phases) {
            subConditions.clear();
            if (searchAreaChecked[SEARCH_AREA_NAME])
                subConditions.add(Condition.property(CardRepository.SQL_CARD_NAME).like(phase));
            if (searchAreaChecked[SEARCH_AREA_CHAR])
                subConditions.add(Condition.property(CardRepository.SQL_CARD_FIRST_CHR).like(phase)
                        .or(Condition.property(CardRepository.SQL_CARD_SECOND_CHR).like(phase))
                );
            if (searchAreaChecked[SEARCH_AREA_TEXT])
                subConditions.add(Condition.property(CardRepository.SQL_CARD_TXT).like(phase));
            if (searchAreaChecked[SEARCH_AREA_SERIAL])
                subConditions.add(Condition.property(CardRepository.SQL_CARD_SERIAL).like(phase));
            Condition condition = null;
            for (Condition subCondition : subConditions) {
                if (condition == null)
                    condition = subCondition;
                else
                    orConditions(condition, subCondition);
            }
            if (condition != null)
                conditions.add(condition);
        }

        Condition resultCondition = null;
        for (Condition condition : conditions) {
            if (resultCondition == null)
                resultCondition = condition;
            else
                resultCondition = andConditions(resultCondition, condition);
        }
        return resultCondition;
    }
}
