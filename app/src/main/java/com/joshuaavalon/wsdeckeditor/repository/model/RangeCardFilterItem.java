package com.joshuaavalon.wsdeckeditor.repository.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.joshuaavalon.fluentquery.Condition;
import com.joshuaavalon.fluentquery.Property;
import com.joshuaavalon.wsdeckeditor.Handler;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.StringResource;
import com.joshuaavalon.wsdeckeditor.StringUtils;
import com.joshuaavalon.wsdeckeditor.WsApplication;

public class RangeCardFilterItem extends CardFilterItem {
    @StringRes
    private final int titleResId;
    @NonNull
    private final String field;
    private int value;
    private Operator operator;
    private Spinner spinner;
    private EditText editText;

    public RangeCardFilterItem(final int titleResId,
                               @NonNull final String field) {
        this.titleResId = titleResId;
        this.field = field;
        value = -1;
        operator = Operator.Equal;
    }

    @NonNull
    @Override
    public Optional<Condition> toCondition() {
        if (value < 0) return Optional.absent();
        final String intStr = String.valueOf(value);
        final Property property = Condition.property(field);
        Condition condition;
        switch (operator) {
            case Less:
                condition = property.lesserThan(intStr);
                break;
            case LessOrEqual:
                condition = property.lesserThanOrEqual(intStr);
                break;
            case Equal:
                condition = property.equal(intStr);
                break;
            case Greater:
                condition = property.greaterThan(intStr);
                break;
            case GreaterOrEqual:
                condition = property.greaterThanOrEqual(intStr);
                break;
            default:
                throw new IllegalArgumentException();
        }
        return Optional.of(condition);
    }

    @NonNull
    @Override
    public MaterialDialog getDialog(@NonNull final Context context,
                                    @NonNull final Handler<Void> callback) {
        final MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title(getTitle())
                .customView(R.layout.dialog_range, false)
                .positiveText(R.string.confirm_button)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        operator = Operator.values()[spinner.getSelectedItemPosition()];
                        final String input = editText.getText().toString();
                        if (Strings.isNullOrEmpty(input))
                            value = -1;
                        else
                            value = Integer.valueOf(input);
                        callback.handle(null);
                    }
                })
                .show();
        final View view = dialog.getCustomView();
        if (view != null) {
            spinner = (Spinner) view.findViewById(R.id.spinner);
            spinner.setAdapter(new ArrayAdapter<>(context,
                    android.R.layout.simple_spinner_dropdown_item,
                    StringUtils.getStringResourceList(Operator.class)));
            final Operator[] values = Operator.values();
            for (int i = 0; i < values.length; i++)
                if (operator == values[i]) {
                    spinner.setSelection(i);
                    break;
                }
            editText = (EditText) view.findViewById(R.id.edit_text);
            if (value >= 0)
                editText.setText(String.valueOf(value));
        }
        return dialog;
    }

    @Override
    public int getTitle() {
        return titleResId;
    }

    @NonNull
    @Override
    public String getContent() {
        return WsApplication.getContext().getString(operator.getResId()) + " " + value;
    }

    public enum Operator implements StringResource {
        Less(R.string.op_lesser),
        LessOrEqual(R.string.op_lesser_eq),
        Equal(R.string.op_eq),
        Greater(R.string.op_greater),
        GreaterOrEqual(R.string.op_greater_eq);
        @StringRes
        private final int resId;

        Operator(@StringRes final int resId) {
            this.resId = resId;
        }

        @Override
        @StringRes
        public int getResId() {
            return resId;
        }

    }

    
}