package com.joshuaavalon.wsdeckeditor.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.joshuaavalon.wsdeckeditor.Handler;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.Utility;
import com.joshuaavalon.wsdeckeditor.WsApplication;
import com.joshuaavalon.wsdeckeditor.fragment.DeckEditFragment;
import com.joshuaavalon.wsdeckeditor.fragment.DeckListFragment;
import com.joshuaavalon.wsdeckeditor.fragment.ExpansionFragment;
import com.joshuaavalon.wsdeckeditor.fragment.SearchFragment;
import com.joshuaavalon.wsdeckeditor.fragment.SettingFragment;
import com.joshuaavalon.wsdeckeditor.model.Deck;
import com.joshuaavalon.wsdeckeditor.model.DeckUtils;
import com.joshuaavalon.wsdeckeditor.repository.CardRepository;
import com.joshuaavalon.wsdeckeditor.repository.DeckRepository;
import com.joshuaavalon.wsdeckeditor.repository.NetworkRepository;
import com.joshuaavalon.wsdeckeditor.repository.model.CardFilterItem;

import java.util.ArrayList;

public class MainActivity extends BaseActivity implements Transactable,
        NavigationView.OnNavigationItemSelectedListener {
    public static final String SEARCH_FILTER = "search_filter";
    public static final int REQUEST_CODE_CARD_DETAIL = 0;
    public static final int REQUEST_CODE_CAMERA = 1;
    private static final int MAX_BACK_STACK_COUNT = 10;
    private ArrayList<CardFilterItem> cardFilterItems = null;
    private long deckId = Deck.NO_ID;
    private Toolbar toolbar;

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

    private void scanQr() {
        if (!Utility.requestPermission(this, REQUEST_CODE_CAMERA, Manifest.permission.CAMERA))
            return;
        final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (manager.getCameraIdList().length > 0)
                new IntentIntegrator(this).initiateScan();
            else
                showMessage(R.string.no_camera);
        } catch (CameraAccessException ignored) {
            showMessage(R.string.camera_error);
        }
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;
        actionBar.setDisplayShowTitleEnabled(false);
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    public void setToolbarOnClick(@Nullable final View.OnClickListener listener) {
        toolbar.setOnClickListener(listener);
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
            case R.id.nav_setting:
                fragment = new SettingFragment();
                break;
            case R.id.nav_qr:
                scanQr();
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
        final IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            final Uri uri = Uri.parse(scanResult.getContents());
            final String scheme = uri.getScheme();
            final String host = uri.getHost();
            if (Strings.isNullOrEmpty(scheme) || Strings.isNullOrEmpty(host) ||
                    !scheme.equals(WsApplication.QR_SCHEME)) {
                showMessage(R.string.err_invalid_er);
            } else {
                final Optional<Deck> deckOptional = DeckUtils.decodeDeck(host);
                if (!deckOptional.isPresent())
                    showMessage(R.string.err_invalid_er);
                else {
                    final Deck deck = deckOptional.get();
                    DeckRepository.save(deck);
                    deckId = deck.getId();
                }
            }

            return;
        }
        if (requestCode != REQUEST_CODE_CARD_DETAIL) return;
        if (resultCode == Activity.RESULT_OK && data != null) {
            cardFilterItems = data.getParcelableArrayListExtra(SEARCH_FILTER);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Fragment fragment = null;
        if (cardFilterItems != null) {
            fragment = SearchFragment.newInstance(cardFilterItems);
            cardFilterItems = null;
        }
        if (deckId != Deck.NO_ID) {
            fragment = DeckEditFragment.newInstance(deckId);
            deckId = Deck.NO_ID;
        }
        if (fragment != null)
            transactTo(fragment);
    }


    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Utility.permissionGranted(requestCode, REQUEST_CODE_CAMERA, grantResults))
            scanQr();
    }
}
