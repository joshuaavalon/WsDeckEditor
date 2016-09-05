package com.joshuaavalon.wsdeckeditor.repository.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.base.Optional;
import com.joshuaavalon.fluentquery.Condition;
import com.joshuaavalon.wsdeckeditor.Handler;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.StringUtils;
import com.joshuaavalon.wsdeckeditor.model.Card;
import com.joshuaavalon.wsdeckeditor.repository.CardRepository;

public class TypeCardFilterItem extends CardFilterItem {
    private Card.Type type = null;

    @NonNull
    @Override
    public Optional<Condition> toCondition() {
        if (type == null) return Optional.absent();
        return Optional.of(Condition.property(CardRepository.SQL_CARD_TYPE).equal(type.toString()));
    }

    @NonNull
    @Override
    public MaterialDialog getDialog(@NonNull final Context context,
                                    @NonNull final Handler<Void> callback) {
        return new MaterialDialog.Builder(context)
                .title(getTitle())
                .items(StringUtils.getStringResourceList(Card.Type.class))
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog,
                                            View itemView,
                                            int position,
                                            CharSequence text) {
                        type = Card.Type.values()[position];
                        callback.handle(null);
                    }
                })
                .show();
    }

    @Override
    public int getTitle() {
        return R.string.card_type;
    }

    @NonNull
    @Override
    public String getContent() {
        return type.toString();
    }
}