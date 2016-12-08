package com.joshuaavalon.wsdeckeditor.view.tab;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.joshuaavalon.wsdeckeditor.fragment.CardDetailFragment;

import java.util.List;

public class CardPagerAdapter extends FragmentStatePagerAdapter {
    @NonNull
    private final List<String> serials;

    public CardPagerAdapter(FragmentManager fm, @NonNull final List<String> serials) {
        super(fm);
        this.serials = serials;
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

    @NonNull
    public List<String> getSerials() {
        return serials;
    }
}
