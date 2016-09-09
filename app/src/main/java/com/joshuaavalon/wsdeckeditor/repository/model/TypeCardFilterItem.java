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

public class TypeCardFilterItem extends CardFilterItem {
    public static final Creator<TypeCardFilterItem> CREATOR = new Creator<TypeCardFilterItem>() {
        @Override
        public TypeCardFilterItem createFromParcel(Parcel source) {
            return new TypeCardFilterItem(source);
        }

        @Override
        public TypeCardFilterItem[] newArray(int size) {
            return new TypeCardFilterItem[size];
        }
    };
    private Card.Type type = null;

    public TypeCardFilterItem() {
    }

    protected TypeCardFilterItem(Parcel in) {
        int tmpType = in.readInt();
        this.type = tmpType == -1 ? null : Card.Type.values()[tmpType];
    }

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
        return R.string.filter_dialog_type;
    }

    @NonNull
    @Override
    public String getContent() {
        return type.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypeCardFilterItem)) return false;
        final TypeCardFilterItem that = (TypeCardFilterItem) o;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        return type != null ? type.hashCode() : 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type == null ? -1 : this.type.ordinal());
    }
}