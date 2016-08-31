package com.joshuaavalon.wsdeckeditor.view;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.joshuaavalon.wsdeckeditor.fragment.CardDetailFragment;

import java.util.ArrayList;
import java.util.List;

public class CardPagerAdapter extends FragmentPagerAdapter {
    @NonNull
    private final List<String> serials;

    public CardPagerAdapter(@NonNull final FragmentManager fragmentManager, @NonNull final List<String> serials) {
        super(fragmentManager);
        this.serials = new ArrayList<>(serials);
    }

    @Override
    public Fragment getItem(int position) {
        return CardDetailFragment.newInstance(serials.get(position));
    }

    @Override
    public int getCount() {
        return serials.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return serials.get(position);
    }
}
