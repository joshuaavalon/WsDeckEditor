package com.joshuaavalon.wsdeckeditor.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.common.base.Objects;
import com.joshuaavalon.android.view.ContentView;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.fragment.AboutFragment;
import com.joshuaavalon.wsdeckeditor.fragment.ExpansionFragment;
import com.joshuaavalon.wsdeckeditor.fragment.SettingFragment;
import com.joshuaavalon.wsdeckeditor.fragment.UpdateFragment;

import butterknife.BindView;
import timber.log.Timber;

@ContentView(R.layout.activity_main)
public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    @BindView(R.id.nav_view)
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        navigationView.setNavigationItemSelectedListener(this);
        fragmentTransaction(new AboutFragment());
        if (getPreference().getFirstTime()) {
            getPreference().setFirstTime(false);
            Timber.i("First time usage.");
        }
        checkUpdate();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.nav_about:
                fragmentTransaction(new AboutFragment());
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
                SearchActivity.start(this, null);
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
        final Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment);
        if (currentFragment == null || !Objects.equal(currentFragment.getClass(), fragment.getClass()))
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, fragment, null)
                    .commit();
    }
}
