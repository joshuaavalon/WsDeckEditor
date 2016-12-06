package com.joshuaavalon.wsdeckeditor.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.util.LruCache;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;

import com.joshuaavalon.android.view.AbstractActivity;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.WsApplication;
import com.joshuaavalon.wsdeckeditor.config.PreferenceRepository;
import com.joshuaavalon.wsdeckeditor.sdk.card.ICardRepository;
import com.joshuaavalon.wsdeckeditor.sdk.deck.IDeckRepository;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class BaseActivity extends AbstractActivity {
    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @Nullable
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @Nullable
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        initializeToolbar();
        initializeDrawerLayout();
    }

    private void initializeToolbar() {
        if (toolbar == null) return;
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;
        actionBar.setDisplayShowTitleEnabled(true);
    }

    private void initializeDrawerLayout() {
        if (drawerLayout == null || toolbar == null) return;
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    protected WsApplication application() {
        return (WsApplication) getApplication();
    }

    protected LruCache<String, Bitmap> getBitmapCache() {
        return application().getBitmapCache();
    }

    protected ICardRepository getCardRepository() {
        return application().getCardRepository();
    }

    protected IDeckRepository getDeckRepository() {
        return application().getDeckRepository();
    }

    protected PreferenceRepository getPreference() {
        return application().getPreference();
    }
}
