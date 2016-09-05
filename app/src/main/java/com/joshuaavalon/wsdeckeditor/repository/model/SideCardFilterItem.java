package com.joshuaavalon.wsdeckeditor.repository.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.base.Optional;
import com.joshuaavalon.fluentquery.Condition;
import com.joshuaavalon.wsdeckeditor.Handler;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.model.Card;
import com.joshuaavalon.wsdeckeditor.repository.CardRepository;

public class SideCardFilterItem extends CardFilterItem {
    private Card.Side side = null;

    @NonNull
    @Override
    public Optional<Condition> toCondition() {
        if (side == null) return Optional.absent();
        return Optional.of(Condition.property(CardRepository.SQL_CARD_SIDE).equal(side.toString()));
    }

    @NonNull
    @Override
    public MaterialDialog getDialog(@NonNull final Context context,
                                    @NonNull final Handler<Void> callback) {
        return new MaterialDialog.Builder(context)
                .title(R.string.card_side)
                .items(R.array.card_side)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog,
                                            View itemView,
                                            int position,
                                            CharSequence text) {
                        side = position == 0 ? Card.Side.W : Card.Side.S;
                        callback.handle(null);
                    }
                })
                .show();
    }

    @Override
    public int getTitle() {
        return R.string.card_side;
    }

    @NonNull
    @Override
    public String getContent() {
        return side.toString();
    }
}
