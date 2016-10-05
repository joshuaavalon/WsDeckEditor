package com.joshuaavalon.wsdeckeditor;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.joshuaavalon.wsdeckeditor.fragment.AboutFragment;
import com.joshuaavalon.wsdeckeditor.fragment.DeckListFragment;
import com.joshuaavalon.wsdeckeditor.fragment.ExpansionFragment;
import com.joshuaavalon.wsdeckeditor.fragment.SearchFragment;
import com.joshuaavalon.wsdeckeditor.fragment.SettingFragment;
import com.joshuaavalon.wsdeckeditor.fragment.UpdateFragment;

public class MainActivity extends AppCompatActivity implements SnackBarSupport,
        NavigationView.OnNavigationItemSelectedListener {
    private static final String INITIAL_STACK = "MainActivity.InitialStack";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUi();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, new AboutFragment(), null)
                .commit();
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
        actionBar.setDisplayShowTitleEnabled(true);
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
            case R.id.nav_search:
                fragment = new SearchFragment();
                break;
            case R.id.nav_deck_edit:
                fragment = new DeckListFragment();
                break;
            case R.id.nav_setting:
                fragment = new SettingFragment();
                break;
            case R.id.nav_update:
                fragment = new UpdateFragment();
                break;
            case R.id.nav_about:
                getSupportFragmentManager().popBackStack(INITIAL_STACK, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                break;
        }
        if (fragment != null) {
            getSupportFragmentManager().popBackStack(INITIAL_STACK, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, fragment, null)
                    .addToBackStack(INITIAL_STACK)
                    .commit();
        }
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment);
        boolean finish = false;
        if (fragment instanceof OnBackPressedListener)
            finish = ((OnBackPressedListener) fragment).onBackPressed();
        if (finish) return;
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }

    @NonNull
    @Override
    public CoordinatorLayout getCoordinatorLayout() {
        return (CoordinatorLayout) findViewById(R.id.coordinator_layout);
    }

    public void transactTo(@NonNull final Fragment fragment, final boolean addToBackStack) {
        if (addToBackStack) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment, fragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment, fragment)
                    .commit();
        }
    }
}
