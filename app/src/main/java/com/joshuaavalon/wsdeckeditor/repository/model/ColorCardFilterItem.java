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
import com.joshuaavalon.wsdeckeditor.model.Card;
import com.joshuaavalon.wsdeckeditor.repository.CardRepository;

public class ColorCardFilterItem extends CardFilterItem {
    public static final Creator<ColorCardFilterItem> CREATOR = new Creator<ColorCardFilterItem>() {
        @Override
        public ColorCardFilterItem createFromParcel(Parcel source) {
            return new ColorCardFilterItem(source);
        }

        @Override
        public ColorCardFilterItem[] newArray(int size) {
            return new ColorCardFilterItem[size];
        }
    };
    private Card.Color color = null;

    public ColorCardFilterItem() {
    }

    protected ColorCardFilterItem(Parcel in) {
        int tmpColor = in.readInt();
        this.color = tmpColor == -1 ? null : Card.Color.values()[tmpColor];
    }

    @NonNull
    @Override
    public Optional<Condition> toCondition() {
        if (color == null) return Optional.absent();
        return Optional.of(Condition.property(CardRepository.SQL_CARD_COLOR).equal(color.toString()));
    }

    @NonNull
    @Override
    public MaterialDialog getDialog(@NonNull final Context context,
                                    @NonNull final Handler<Void> callback) {
        return new MaterialDialog.Builder(context)
                .title(getTitle())
                .items(StringUtils.getStringResourceList(Card.Color.class))
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog,
                                            View itemView,
                                            int position,
                                            CharSequence text) {
                        color = Card.Color.values()[position];
                        callback.handle(null);
                    }
                })
                .show();
    }

    @Override
    public int getTitle() {
        return R.string.filter_dialog_color;
    }

    @NonNull
    @Override
    public String getContent() {
        return color.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ColorCardFilterItem)) return false;
        ColorCardFilterItem that = (ColorCardFilterItem) o;
        return color == that.color;
    }

    @Override
    public int hashCode() {
        return color != null ? color.hashCode() : 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.color == null ? -1 : this.color.ordinal());
    }
}
