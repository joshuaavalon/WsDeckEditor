package com.joshuaavalon.wsdeckeditor.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import java.util.List;

/**
 * An animated adapter used by {@link RecyclerView}.
 *
 * @param <T>  Type of the data models.
 * @param <VH> {@link BaseRecyclerViewHolder} used by {@link BaseRecyclerAdapter}
 */
public abstract class AnimatedRecyclerAdapter<T, VH extends BaseRecyclerViewHolder<T>>
        extends BaseRecyclerAdapter<T, VH> {
    /**
     * Create a new instance of  {@link AnimatedRecyclerAdapter} and use {@link #models}
     *
     * @param models New data models to be used by this adapter.
     */
    protected AnimatedRecyclerAdapter(@NonNull List<T> models) {
        super(models);
    }

    /**
     * Replace the data models of this adapter with animation.
     *
     * @param models New data models to be used by this adapter.
     */
    public void setModels(@NonNull final List<T> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private T removeItem(int position) {
        final T model = models.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    private void addItem(int position, T model) {
        models.add(position, model);
        notifyItemInserted(position);
    }

    private void moveItem(int fromPosition, int toPosition) {
        final T model = models.remove(fromPosition);
        models.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }

    private void applyAndAnimateRemovals(@NonNull final List<T> newTs) {
        for (int i = models.size() - 1; i >= 0; i--) {
            final T model = models.get(i);
            if (!newTs.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(@NonNull final List<T> newTs) {
        for (int i = 0, count = newTs.size(); i < count; i++) {
            final T model = newTs.get(i);
            if (!models.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(@NonNull final List<T> newTs) {
        for (int toPosition = newTs.size() - 1; toPosition >= 0; toPosition--) {
            final T model = newTs.get(toPosition);
            final int fromPosition = models.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }
}
