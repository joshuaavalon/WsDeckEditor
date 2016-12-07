package com.joshuaavalon.deprecation.exclude.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import java.util.List;

/**
 * An adapter used by {@link RecyclerView}.
 *
 * @param <T>  Type of the data models.
 * @param <VH> {@link BaseRecyclerViewHolder} used by {@link BaseRecyclerAdapter}
 */
public abstract class BaseRecyclerAdapter<T, VH extends BaseRecyclerViewHolder<T>>
        extends RecyclerView.Adapter<VH> {
    protected List<T> models;

    /**
     * Create a new instance of  {@link BaseRecyclerAdapter} and use {@link #models}
     *
     * @param models New data models to be used by this adapter.
     */
    protected BaseRecyclerAdapter(@NonNull final List<T> models) {
        this.models = models;
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should update the contents of the
     * {@link BaseRecyclerViewHolder} to reflect the item at the given position.<br>
     * By default, it uses {@link BaseRecyclerViewHolder#bind(Object)}.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(final VH holder, final int position) {
        holder.bind(models.get(position));
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    /**
     * Replace the data models of this adapter.
     *
     * @param models New data models to be used by this adapter.
     */
    public void setModels(@NonNull final List<T> models) {
        this.models = models;
        notifyDataSetChanged();
    }
}
