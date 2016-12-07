package com.joshuaavalon.wsdeckeditor.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.joshuaavalon.android.view.AbstractFragment;
import com.joshuaavalon.wsdeckeditor.WsApplication;
import com.joshuaavalon.wsdeckeditor.activity.BaseActivity;
import com.joshuaavalon.wsdeckeditor.config.PreferenceRepository;
import com.joshuaavalon.wsdeckeditor.sdk.card.ICardRepository;
import com.joshuaavalon.wsdeckeditor.sdk.deck.IDeckRepository;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public class BaseFragment extends AbstractFragment {
    private Unbinder unbinder;

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
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
        activity().showMessage(resId);
    }

    protected void showMessage(@NonNull final String message) {
        activity().showMessage(message);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
