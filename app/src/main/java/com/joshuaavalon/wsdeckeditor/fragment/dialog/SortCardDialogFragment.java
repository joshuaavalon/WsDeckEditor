package com.joshuaavalon.wsdeckeditor.fragment.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.joshuaavalon.wsdeckeditor.Handler;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.model.Card;

public class SortCardDialogFragment extends DialogFragment {
    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setView(R.layout.dialog_sort_card)
                .setTitle(R.string.sort);
        final AlertDialog dialog = builder.show();
        final ListView listView = (ListView) dialog.findViewById(R.id.list_view);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, getOptions());
        if (listView != null) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    final Fragment targetFragment = getTargetFragment();
                    ((Handler<Card.SortOrder>) targetFragment).handle(getResult(getOptions()[position]));
                    dismiss();
                }
            });
            listView.setAdapter(adapter);
        }
        return dialog;
    }

    public static <T extends Fragment & Handler<?>>
    void start(@NonNull final FragmentManager fragmentManager,
               @NonNull final T targetFragment) {
        final SortCardDialogFragment fragment = new SortCardDialogFragment();
        fragment.setTargetFragment(targetFragment, 0);
        fragment.show(fragmentManager, null);
    }

    @NonNull
    private String[] getOptions() {
        return getResources().getStringArray(R.array.sort_type);
    }

    private Card.SortOrder getResult(@NonNull final String option) {
        if (option.equals(getOptions()[2])) return Card.SortOrder.Detail;
        if (option.equals(getOptions()[1])) return Card.SortOrder.Level;
        return Card.SortOrder.Serial;

    }
}