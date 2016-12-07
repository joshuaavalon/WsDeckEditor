package com.joshuaavalon.wsdeckeditor.view.recycler.viewholder;

import android.view.View;

import com.joshuaavalon.android.view.recyclerview.BindingViewHolder;

import butterknife.ButterKnife;

public abstract class BaseViewHolder<T> extends BindingViewHolder<T> {
    public BaseViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
