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

public class TriggerCardFilterItem extends CardFilterItem {
    public static final Creator<TriggerCardFilterItem> CREATOR = new Creator<TriggerCardFilterItem>() {
        @Override
        public TriggerCardFilterItem createFromParcel(Parcel source) {
            return new TriggerCardFilterItem(source);
        }

        @Override
        public TriggerCardFilterItem[] newArray(int size) {
            return new TriggerCardFilterItem[size];
        }
    };
    private Card.Trigger trigger = null;

    public TriggerCardFilterItem() {
    }

    protected TriggerCardFilterItem(Parcel in) {
        int tmpTrigger = in.readInt();
        this.trigger = tmpTrigger == -1 ? null : Card.Trigger.values()[tmpTrigger];
    }

    @NonNull
    @Override
    public Optional<Condition> toCondition() {
        if (trigger == null) return Optional.absent();
        return Optional.of(Condition.property(CardRepository.SQL_CARD_TYPE).equal(trigger.toString()));
    }

    @NonNull
    @Override
    public MaterialDialog getDialog(@NonNull final Context context,
                                    @NonNull final Handler<Void> callback) {
        return new MaterialDialog.Builder(context)
                .title(getTitle())
                .items(StringUtils.getStringResourceList(Card.Trigger.class))
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog,
                                            View itemView,
                                            int position,
                                            CharSequence text) {
                        trigger = Card.Trigger.values()[position];
                        callback.handle(null);
                    }
                })
                .show();
    }

    @Override
    public int getTitle() {
        return R.string.card_trigger;
    }

    @NonNull
    @Override
    public String getContent() {
        return trigger.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TriggerCardFilterItem)) return false;
        final TriggerCardFilterItem that = (TriggerCardFilterItem) o;
        return trigger == that.trigger;
    }

    @Override
    public int hashCode() {
        return trigger != null ? trigger.hashCode() : 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.trigger == null ? -1 : this.trigger.ordinal());
    }
}