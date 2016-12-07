package com.joshuaavalon.wsdeckeditor.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.joshuaavalon.android.view.AbstractFragment;
import com.joshuaavalon.wsdeckeditor.WsApplication;
import com.joshuaavalon.wsdeckeditor.activity.BaseActivity;
import com.joshuaavalon.wsdeckeditor.config.PreferenceRepository;
import com.joshuaavalon.wsdeckeditor.sdk.card.ICardRepository;
import com.joshuaavalon.wsdeckeditor.sdk.deck.IDeckRepository;

import butterknife.ButterKnife;

public class BaseFragment extends AbstractFragment {
    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getTitle());
    }

    @NonNull
    public String getTitle() {
        return "";
    }

    @NonNull
    public BaseActivity activity() {
        return (BaseActivity) getActivity();
    }

    public WsApplication application() {
        return activity().application();
    }

    public ICardRepository getCardRepository() {
        return activity().getCardRepository();
    }

    public IDeckRepository getDeckRepository() {
        return activity().getDeckRepository();
    }

    public PreferenceRepository getPreference() {
        return activity().getPreference();
    }

    protected void showMessage(@StringRes final int resId) {
        final CoordinatorLayout coordinatorLayout = activity().getCoordinatorLayout();
        if (coordinatorLayout != null)
            Snackbar.make(coordinatorLayout, resId, Snackbar.LENGTH_LONG).show();
        else
            Toast.makeText(getActivity(), resId, Toast.LENGTH_LONG).show();
    }

    protected void showMessage(@NonNull final String message) {
        final CoordinatorLayout coordinatorLayout = activity().getCoordinatorLayout();
        if (coordinatorLayout != null)
            Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
        else
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }
}
