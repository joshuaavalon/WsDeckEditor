package com.joshuaavalon.wsdeckeditor.exclude.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.joshuaavalon.wsdeckeditor.R;

public class AboutFragment extends BaseFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_about, container, false);
        final TextView textView = (TextView) view.findViewById(R.id.text_view);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        return view;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getString(R.string.nav_about);
    }
}
