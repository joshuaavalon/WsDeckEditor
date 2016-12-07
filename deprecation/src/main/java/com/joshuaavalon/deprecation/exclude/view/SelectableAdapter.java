package com.joshuaavalon.deprecation.exclude.view;

import android.support.annotation.NonNull;
import android.util.SparseBooleanArray;

import java.util.ArrayList;
import java.util.List;

public abstract class SelectableAdapter<T, VH extends BaseRecyclerViewHolder<T>>
        extends AnimatedRecyclerAdapter<T, VH> {
    @NonNull
    private final SparseBooleanArray selectedItems;

    protected SelectableAdapter(@NonNull final List<T> models) {
        super(models);
        selectedItems = new SparseBooleanArray();
    }

    public boolean isSelected(final int position) {
        return getSelectedItems().contains(position);
    }

    public void toggleSelection(final int position) {
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position);
        } else {
            selectedItems.put(position, true);
        }
        notifyItemChanged(position);
    }

    public void clearSelection() {
        final List<Integer> selection = getSelectedItems();
        selectedItems.clear();
        for (Integer i : selection) {
            notifyItemChanged(i);
        }
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    @NonNull
    public List<Integer> getSelectedItems() {
        final List<Integer> items = new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); ++i) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    public void selectAll() {
        for (int i = 0; i < models.size(); i++) {
            selectedItems.put(i, true);
            notifyItemChanged(i);
        }
    }
}
