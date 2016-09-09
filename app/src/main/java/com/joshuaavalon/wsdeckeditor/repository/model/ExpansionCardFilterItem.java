package com.joshuaavalon.wsdeckeditor.repository.model;

import android.content.Context;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.base.Optional;
import com.joshuaavalon.fluentquery.Condition;
import com.joshuaavalon.wsdeckeditor.Handler;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.repository.CardRepository;

import java.util.List;

public class ExpansionCardFilterItem extends CardFilterItem {
    public static final Creator<ExpansionCardFilterItem> CREATOR = new Creator<ExpansionCardFilterItem>() {
        @Override
        public ExpansionCardFilterItem createFromParcel(Parcel source) {
            return new ExpansionCardFilterItem(source);
        }

        @Override
        public ExpansionCardFilterItem[] newArray(int size) {
            return new ExpansionCardFilterItem[size];
        }
    };
    private String expansion;

    public ExpansionCardFilterItem() {
    }

    protected ExpansionCardFilterItem(Parcel in) {
        this.expansion = in.readString();
    }

    public void setExpansion(String expansion) {
        this.expansion = expansion;
    }

    @NonNull
    @Override
    public Optional<Condition> toCondition() {
        if (expansion == null) return Optional.absent();
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
        return R.string.filter_dialog_expansion;
    }

    @NonNull
    @Override
    public String getContent() {
        return expansion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExpansionCardFilterItem)) return false;
        final ExpansionCardFilterItem that = (ExpansionCardFilterItem) o;
        return expansion != null ? expansion.equals(that.expansion) : that.expansion == null;
    }

    @Override
    public int hashCode() {
        return expansion != null ? expansion.hashCode() : 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.expansion);
    }
}
