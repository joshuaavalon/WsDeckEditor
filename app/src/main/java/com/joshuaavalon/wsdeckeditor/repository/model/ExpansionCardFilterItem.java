package com.joshuaavalon.wsdeckeditor.repository.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.base.Optional;
import com.joshuaavalon.fluentquery.Condition;
import com.joshuaavalon.wsdeckeditor.Handler;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.repository.CardRepository;

import java.util.List;

public class ExpansionCardFilterItem extends CardFilterItem{
    private String expansion;

    @NonNull
    @Override
    public Optional<Condition> toCondition() {
        if(expansion == null) return  Optional.absent();
        return Optional.of(Condition.property(CardRepository.SQL_CARD_EXP).equal(expansion));
    }

    @NonNull
    @Override
    public MaterialDialog getDialog(Context context, final Handler<Void> callback) {
        final List<String> expansions = CardRepository.getExpansions();
        return new MaterialDialog.Builder(context)
                .title(getTitle())
                .items(expansions)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog,
                                            View itemView,
                                            int position,
                                            CharSequence text) {
                        expansion = expansions.get(position);
                        callback.handle(null);

                    }
                })
                .show();
    }

    @Override
    public int getTitle() {
        return R.string.card_expansion;
    }

    @NonNull
    @Override
    public String getContent() {
        return expansion;
    }
}
