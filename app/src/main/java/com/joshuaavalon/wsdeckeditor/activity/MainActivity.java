package com.joshuaavalon.wsdeckeditor.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.joshuaavalon.wsdeckeditor.Handler;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.fragment.DeckListFragment;
import com.joshuaavalon.wsdeckeditor.fragment.ExpansionFragment;
import com.joshuaavalon.wsdeckeditor.fragment.SearchFragment;
import com.joshuaavalon.wsdeckeditor.repository.CardRepository;
import com.joshuaavalon.wsdeckeditor.repository.NetworkRepository;
import com.joshuaavalon.wsdeckeditor.repository.model.CardFilterItem;

import java.util.ArrayList;

public class MainActivity extends BaseActivity implements Transactable,
        NavigationView.OnNavigationItemSelectedListener {
    public static final String SEARCH_FILTER = "search_filter";
    public static final int CARD_DETAIL_CODE = 0;
    private static final int MAX_BACK_STACK_COUNT = 10;
    private ArrayList<CardFilterItem> cardFilterItems = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUi();
        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        if (savedInstanceState == null && findViewById(R.id.frame_content) != null) {
            //transactTo(new DeckInfoFragment(), false);
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
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
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
            case R.id.nav_update:
                showUpdateDialog();
                break;
        }
        if (fragment != null)
            transactTo(fragment);
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void transactTo(@NonNull final Fragment fragment) {
        transactTo(fragment, true);
    }

    public void transactTo(@NonNull Fragment fragment, boolean addToBackStack) {
        if (getSupportFragmentManager().getBackStackEntryCount() > MAX_BACK_STACK_COUNT)
            getSupportFragmentManager().popBackStack(null,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
        if (addToBackStack) {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_right,
                            R.anim.exit_to_left,
                            R.anim.enter_from_left,
                            R.anim.exit_to_right)
                    .replace(R.id.frame_content, fragment, null)
                    .addToBackStack(null)
                    .commit();
        } else
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_content, fragment, null)
                    .commit();
    }

    public void setTitle(@NonNull final String title) {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;
        actionBar.setTitle(title);
        actionBar.setDisplayShowTitleEnabled(true);
    }

    public void removeTitle() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;
        actionBar.setDisplayShowTitleEnabled(false);
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

    private void showUpdateDialog() {
        final MaterialDialog progressDialog = new MaterialDialog.Builder(this)
                .title(R.string.load_version)
                .content(R.string.wait_message)
                .progress(true, 0)
                .show();
        NetworkRepository.downloadVersion(
                new Handler<Integer>() {
                    @Override
                    public void handle(Integer version) {
                        progressDialog.dismiss();
                        showDownloadDialog(version);
                    }
                },
                new Handler<String>() {
                    @Override
                    public void handle(String object) {
                        Log.e("Error", object);
                        progressDialog.dismiss();
                        showMessage(object);
                    }
                });
    }

    private void showDownloadDialog(final int version) {
        final MaterialDialog updateDialog = new MaterialDialog.Builder(this)
                .title(R.string.download_dialog_title)
                .customView(R.layout.dialog_update, false)
                .show();
        final View view = updateDialog.getCustomView();
        if (view == null) return;
        final TextView textView = (TextView) view.findViewById(R.id.text_view);
        textView.setText(getString(
                R.string.latest_version,
                String.valueOf(version),
                String.valueOf(CardRepository.getVersion()
                )));
        final Button updateDatabaseButton = (Button) view.findViewById(R.id.update_database_button);
        updateDatabaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NetworkRepository.downloadDatabase();
                showMessage(R.string.update_started);
                updateDialog.dismiss();
            }
        });
        final Button updateImageButton = (Button) view.findViewById(R.id.update_images_button);
        updateImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NetworkRepository.downloadImages(CardRepository.getAllImages(), false);
                showMessage(R.string.update_started);
                updateDialog.dismiss();
            }
        });
        final Button downloadAllImagesButton = (Button) view.findViewById(R.id.download_all_images_button);
        downloadAllImagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NetworkRepository.downloadImages(CardRepository.getAllImages(), true);
                showMessage(R.string.update_started);
                updateDialog.dismiss();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != CARD_DETAIL_CODE) return;
        if (resultCode == Activity.RESULT_OK && data != null) {
            cardFilterItems = data.getParcelableArrayListExtra(SEARCH_FILTER);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (cardFilterItems == null) return;
        final SearchFragment fragment = SearchFragment.newInstance(cardFilterItems);
        cardFilterItems = null;
        transactTo(fragment);
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
