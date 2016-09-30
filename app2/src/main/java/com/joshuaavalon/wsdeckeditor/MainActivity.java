package com.joshuaavalon.wsdeckeditor;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements SnackBarSupport,
        NavigationView.OnNavigationItemSelectedListener {
    private static final String INITIAL_STACK = "MainActivity.InitialStack";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUi();
        if (savedInstanceState == null && findViewById(R.id.fragment) != null) {
            //transactTo(new DeckInfoFragment(), false);
            //TODO
        }
    }

    private void initUi() {
        initToolbar();
        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void initToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;
        actionBar.setDisplayShowTitleEnabled(false);
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.nav_drawer_open,
                R.string.nav_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        final int id = item.getItemId();
        Fragment fragment = null;
        switch (id) {
            case R.id.nav_card_list:
                fragment = new ExpansionFragment();
                break;
        }
        if (fragment != null) {
            getSupportFragmentManager().popBackStack(INITIAL_STACK, 0);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, fragment, null)
                    .addToBackStack(null)
                    .commit();
        }
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

    @NonNull
    @Override
    public CoordinatorLayout getCoordinatorLayout() {
        return (CoordinatorLayout) findViewById(R.id.coordinator_layout);
    }
}
