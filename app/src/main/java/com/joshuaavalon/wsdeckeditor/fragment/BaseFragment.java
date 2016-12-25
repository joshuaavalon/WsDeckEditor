package com.joshuaavalon.wsdeckeditor.fragment;

import android.content.Context;
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
    private WsApplication application;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BaseActivity)
            application = ((BaseActivity) context).application();
    }

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        final String title = getTitle();
        if (title != null)
            getActivity().setTitle(title);
        return view;
    }

    @Nullable
    public String getTitle() {
        return null;
    }

    @Nullable
    public BaseActivity activity() {
        return (BaseActivity) getActivity();
    }

    public WsApplication application() {
        return application;
    }

    public ICardRepository getCardRepository() {
        return application().getCardRepository();
    }

    public IDeckRepository getDeckRepository() {
        return application().getDeckRepository();
    }

    public PreferenceRepository getPreference() {
        return application().getPreference();
    }

    protected void showMessage(@StringRes final int resId) {
        final BaseActivity activity = activity();
        if (activity != null)
            activity.showMessage(resId);
    }

    protected void showMessage(@NonNull final String message) {
        final BaseActivity activity = activity();
        if (activity != null)
            activity.showMessage(message);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
