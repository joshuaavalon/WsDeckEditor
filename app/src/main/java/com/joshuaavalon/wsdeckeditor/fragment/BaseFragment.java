package com.joshuaavalon.wsdeckeditor.fragment;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.joshuaavalon.wsdeckeditor.SnackBarSupport;

public abstract class BaseFragment extends Fragment {
    @NonNull
    public String getTitle() {
        return "";
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getTitle());
    }

    protected void showMessage(@StringRes final int resId) {
        final Activity activity = getActivity();
        if (activity == null) return;
        if (activity instanceof SnackBarSupport) {
            Snackbar.make(((SnackBarSupport) activity).getCoordinatorLayout(),
                    resId, Snackbar.LENGTH_LONG).show();
        } else
            Toast.makeText(getActivity(), resId, Toast.LENGTH_LONG).show();
    }

    protected void showMessage(@NonNull final String message) {
        final Activity activity = getActivity();
        if (activity == null) return;
        if (activity instanceof SnackBarSupport) {
            Snackbar.make(((SnackBarSupport) activity).getCoordinatorLayout(),
                    message, Snackbar.LENGTH_LONG).show();
        } else
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }
}
