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
import com.joshuaavalon.android.view.ContentView;
import com.joshuaavalon.wsdeckeditor.R;

import butterknife.BindView;

@ContentView(R.layout.activity_main)
public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    @BindView(R.id.nav_view)
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        navigationView.setNavigationItemSelectedListener(this);
        checkUpdate();
        //TODO: Setup First Time Visit
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        final int id = item.getItemId();
        Fragment fragment = null;
        switch (id) {
            case R.id.nav_card_list:
                break;
            default:
                return false;
        }
        if (drawerLayout != null)
            drawerLayout.closeDrawer(GravityCompat.START);
        return false;
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
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }
}
