package com.joshuaavalon.wsdeckeditor.repository.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.base.Optional;
import com.joshuaavalon.fluentquery.Condition;
import com.joshuaavalon.wsdeckeditor.Handler;

public abstract class CardFilterItem implements Parcelable {
    @NonNull
    public abstract Optional<Condition> toCondition();

    @NonNull
    public abstract MaterialDialog getDialog(Context context, Handler<Void> callback);

    @StringRes
    public abstract int getTitle();

    @NonNull
    public abstract String getContent();

    @NonNull
    protected static Condition andConditions(@NonNull final Condition left,
                                             @Nullable final Condition right) {
        if (right == null)
            return left;
        return left.and(right);
    }

    @NonNull
    protected static Condition orConditions(@NonNull final Condition left,
                                            @Nullable final Condition right) {
        if (right == null)
            return left;
        return left.or(right);
    }
}
