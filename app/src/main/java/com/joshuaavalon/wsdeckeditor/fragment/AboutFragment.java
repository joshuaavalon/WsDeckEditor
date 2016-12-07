package com.joshuaavalon.wsdeckeditor.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.joshuaavalon.android.view.ContentView;
import com.joshuaavalon.wsdeckeditor.R;

import butterknife.ButterKnife;

@ContentView(R.layout.fragment_about)
public class AboutFragment extends BaseFragment {
    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        final TextView textView = ButterKnife.findById(view, R.id.text_view);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        return view;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getString(R.string.nav_about);
    }
}
