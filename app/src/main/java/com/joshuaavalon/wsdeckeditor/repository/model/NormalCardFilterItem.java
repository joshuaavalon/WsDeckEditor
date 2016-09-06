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

public class NormalCardFilterItem extends CardFilterItem {
    public static final Creator<NormalCardFilterItem> CREATOR = new Creator<NormalCardFilterItem>() {
        @Override
        public NormalCardFilterItem createFromParcel(Parcel source) {
            return new NormalCardFilterItem(source);
        }

        @Override
        public NormalCardFilterItem[] newArray(int size) {
            return new NormalCardFilterItem[size];
        }
    };
    private boolean normalOnly;
    private boolean isInit;

    public NormalCardFilterItem() {
        normalOnly = false;
        isInit = false;
    }

    protected NormalCardFilterItem(Parcel in) {
        this.normalOnly = in.readByte() != 0;
        this.isInit = in.readByte() != 0;
    }

    public void setNormalOnly(boolean normalOnly) {
        this.normalOnly = normalOnly;
        isInit = true;
    }

    @NonNull
    @Override
    public Optional<Condition> toCondition() {
        if (!isInit) return Optional.absent();
        final Condition conditionSR = Condition.property(CardRepository.SQL_CARD_RARITY).equal("SR");
        final Condition conditionSP = Condition.property(CardRepository.SQL_CARD_RARITY).equal("SP");
        final Condition conditionRRR = Condition.property(CardRepository.SQL_CARD_RARITY).equal("RRR");
        final Condition conditionXR = Condition.property(CardRepository.SQL_CARD_RARITY).equal("XR");
        return Optional.of(conditionSR.or(conditionSP.or(conditionRRR.or(conditionXR))).not());
    }

    @NonNull
    @Override
    public MaterialDialog getDialog(@NonNull final Context context,
                                    @NonNull final Handler<Void> callback) {
        return new MaterialDialog.Builder(context)
                .title(getTitle())
                .items(R.array.card_normal)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        isInit = true;
                        normalOnly = position == 0;
                        callback.handle(null);
                    }
                })
                .show();
    }

    @Override
    public int getTitle() {
        return R.string.normal_only;
    }

    @NonNull
    @Override
    public String getContent() {
        return String.valueOf(normalOnly);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NormalCardFilterItem)) return false;
        final NormalCardFilterItem that = (NormalCardFilterItem) o;
        return normalOnly == that.normalOnly && isInit == that.isInit;

    }

    @Override
    public int hashCode() {
        int result = (normalOnly ? 1 : 0);
        result = 31 * result + (isInit ? 1 : 0);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.normalOnly ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isInit ? (byte) 1 : (byte) 0);
    }
}