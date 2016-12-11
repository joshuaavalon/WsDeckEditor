package com.joshuaavalon.wsdeckeditor.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.common.base.Objects;
import com.joshuaavalon.android.view.ContentView;
import com.joshuaavalon.android.view.fab.ScrollAwareFabBehavior;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.fragment.DeckListFragment;
import com.joshuaavalon.wsdeckeditor.fragment.ExpansionFragment;
import com.joshuaavalon.wsdeckeditor.fragment.HomeFragment;
import com.joshuaavalon.wsdeckeditor.fragment.SettingFragment;
import com.joshuaavalon.wsdeckeditor.fragment.UpdateFragment;

import butterknife.BindView;
import timber.log.Timber;

@ContentView(R.layout.activity_main)
public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeDrawerLayout();
        navigationView.setNavigationItemSelectedListener(this);
        fragmentTransaction(new HomeFragment(), false);
        if (getPreference().getFirstTime()) {
            getPreference().setFirstTime(false);
            Timber.i("First time usage.");
        }
        checkUpdate();
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                View view = MainActivity.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
    }

    private void initializeDrawerLayout() {
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.nav_home:
                fragmentTransaction(new HomeFragment());
                break;
            case R.id.nav_update:
                fragmentTransaction(new UpdateFragment());
                break;
            case R.id.nav_setting:
                fragmentTransaction(new SettingFragment());
                break;
            case R.id.nav_card_list:
                fragmentTransaction(new ExpansionFragment());
                break;
            case R.id.nav_search:
                SearchActivity.start(this, null, coordinatorLayout);
                break;
            case R.id.nav_deck_edit:
                fragmentTransaction(new DeckListFragment());
                break;
            default:
                return false;
        }
        if (drawerLayout != null)
            drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void enableUpdateNotification(boolean show) {
        navigationView.getMenu().findItem(R.id.nav_update).getActionView()
                .setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void checkUpdate() {
        getCardRepository().needUpdated(
                new Response.Listener<Boolean>() {
                    @Override
                    public void onResponse(Boolean response) {
                        if (!response)
                            checkAppVersion();
                        else
                            enableUpdateNotification(true);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        checkAppVersion();
                    }
                });
    }

    private void checkAppVersion() {
        getPreference().needUpdated(
                new Response.Listener<Boolean>() {
                    @Override
                    public void onResponse(Boolean response) {
                        enableUpdateNotification(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        enableUpdateNotification(false);
                    }
                });
    }

    @Override
    public void onBackPressed() {
        // Close drawer if it is opened
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }

    private void fragmentTransaction(@NonNull final Fragment fragment) {
        fragmentTransaction(fragment, true);
    }

    private void fragmentTransaction(@NonNull final Fragment fragment, final boolean anim) {
        if (fragment instanceof DeckListFragment) {
            final CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
            params.setBehavior(new ScrollAwareFabBehavior(this, null));
            fab.requestLayout();
            fab.show();
        } else {
            final CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
            params.setBehavior(null);
            fab.requestLayout();
            fab.hide();
        }
        final Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment);
        if (currentFragment != null && Objects.equal(currentFragment.getClass(), fragment.getClass()))
            return;
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (anim)
            transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right,
                    R.anim.enter_from_right, R.anim.exit_to_left);
        transaction.replace(R.id.fragment, fragment, null).commit();
    }
}
