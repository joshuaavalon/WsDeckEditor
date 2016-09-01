package com.joshuaavalon.wsdeckeditor.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.fragment.CardDetailFragment;
import com.joshuaavalon.wsdeckeditor.fragment.DeckListFragment;
import com.joshuaavalon.wsdeckeditor.fragment.ExpansionFragment;
import com.joshuaavalon.wsdeckeditor.fragment.SearchFragment;

public class MainActivity extends BaseActivity implements Transactable,
        NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUi();
        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        if (savedInstanceState == null && findViewById(R.id.frame_content) != null) {
            CardDetailFragment homeFragment = CardDetailFragment.newInstance("DC/W01-001");
            transactTo(homeFragment, false);
        }
    }

    private void initUi() {
        initToolbar();
    }

    private void initToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;
        actionBar.setDisplayShowTitleEnabled(false);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        final int id = item.getItemId();
        Fragment fragment = null;
        switch (id) {
            case R.id.nav_cardlist:
                fragment = new ExpansionFragment();
                break;
            case R.id.nav_search:
                fragment = new SearchFragment();
                break;
            case R.id.nav_deckedit:
                fragment = new DeckListFragment();
                break;
        }
        if (fragment != null)
            transactTo(fragment);
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void transactTo(@NonNull final Fragment fragment) {
        transactTo(fragment, true);
    }

    @Override
    public void transactTo(@NonNull Fragment fragment, boolean addToBackStack) {
        if (addToBackStack)
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_right,
                            R.anim.exit_to_left,
                            R.anim.enter_from_left,
                            R.anim.exit_to_right)
                    .replace(R.id.frame_content, fragment, null)
                    .addToBackStack(null)
                    .commit();
        else
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_content, fragment, null)
                    .commit();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}


/*

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Utility.permissionGranted(
                requestCode,
                1,
                grantResults)) {
        }
    }
*/


/*
        Log.e("Version", String.valueOf(CardRepository.getVersion()));
        final CardRepository.Filter filter = new CardRepository.Filter();
        filter.addAnd("CL");
        filter.setMinLevel(0);
        List<Card> cards = CardRepository.getCards(filter);
        for (Card card : cards)
            Log.e("Card", card.getName());
        if (Utility.requestPermission(this, 1, Manifest.permission.CAMERA)) {

            QRCode.decode(this, new Function<Optional<String>, Void>() {
                @Override
                public Void apply(Optional<String> input) {
                    if (input.isPresent())
                        Log.e("QR", input.get());
                    else
                        Log.e("QR", "FFF");
                    return null;
                }
            });
        }
*/
