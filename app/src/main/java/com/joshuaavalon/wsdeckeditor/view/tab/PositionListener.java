package com.joshuaavalon.wsdeckeditor.view.tab;

import android.support.v4.view.ViewPager;

public class PositionListener extends ViewPager.SimpleOnPageChangeListener {
    private int currentPage;

    @Override
    public void onPageSelected(int position) {
        currentPage = position;
    }

    public final int getCurrentPage() {
        return currentPage;
    }
}
