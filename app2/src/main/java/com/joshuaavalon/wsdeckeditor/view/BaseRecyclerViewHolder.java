package com.joshuaavalon.wsdeckeditor.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class BaseRecyclerViewHolder<T> extends RecyclerView.ViewHolder {
    public BaseRecyclerViewHolder(final View itemView) {
        super(itemView);
    }

    /**
     * Bind the data object to {@link BaseRecyclerViewHolder}.
     *
     * @param obj Data to be bind.
     */
    public abstract void bind(T obj);
}
