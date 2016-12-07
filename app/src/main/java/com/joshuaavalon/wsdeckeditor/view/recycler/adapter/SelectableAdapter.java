package com.joshuaavalon.wsdeckeditor.view.recycler.adapter;

import android.support.annotation.NonNull;
import android.util.SparseBooleanArray;

import com.joshuaavalon.android.view.recyclerview.AnimatedRecyclerAdapter;
import com.joshuaavalon.android.view.recyclerview.ViewHolderFactory;

import java.util.ArrayList;
import java.util.List;

public class SelectableAdapter<T> extends AnimatedRecyclerAdapter<T> {
    @NonNull
    private final SparseBooleanArray selectedItems;

    public SelectableAdapter(@NonNull final List<T> models, @NonNull final ViewHolderFactory<T> viewHolderFactory) {
        super(models, viewHolderFactory);
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
