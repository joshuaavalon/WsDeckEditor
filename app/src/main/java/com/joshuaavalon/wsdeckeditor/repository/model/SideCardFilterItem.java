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
import com.joshuaavalon.wsdeckeditor.StringUtils;
import com.joshuaavalon.wsdeckeditor.WsApplication;
import com.joshuaavalon.wsdeckeditor.model.Card;
import com.joshuaavalon.wsdeckeditor.repository.CardRepository;

public class SideCardFilterItem extends CardFilterItem {
    public static final Creator<SideCardFilterItem> CREATOR = new Creator<SideCardFilterItem>() {
        @Override
        public SideCardFilterItem createFromParcel(Parcel source) {
            return new SideCardFilterItem(source);
        }

        @Override
        public SideCardFilterItem[] newArray(int size) {
            return new SideCardFilterItem[size];
        }
    };
    private Card.Side side = null;

    public SideCardFilterItem() {
    }

    protected SideCardFilterItem(Parcel in) {
        int tmpSide = in.readInt();
        this.side = tmpSide == -1 ? null : Card.Side.values()[tmpSide];
    }

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
                .title(getTitle())
                .items(StringUtils.getStringResourceList(Card.Side.class))
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog,
                                            View itemView,
                                            int position,
                                            CharSequence text) {
                        side = Card.Side.values()[position];
                        callback.handle(null);
                    }
                })
                .show();
    }

    @Override
    public int getTitle() {
        return R.string.filter_dialog_side;
    }

    @NonNull
    @Override
    public String getContent() {
        return WsApplication.getContext().getString(side.getResId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SideCardFilterItem)) return false;
        final SideCardFilterItem that = (SideCardFilterItem) o;
        return side == that.side;
    }

    @Override
    public int hashCode() {
        return side != null ? side.hashCode() : 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.side == null ? -1 : this.side.ordinal());
    }
}
