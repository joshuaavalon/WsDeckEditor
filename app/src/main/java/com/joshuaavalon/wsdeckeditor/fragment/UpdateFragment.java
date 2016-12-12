package com.joshuaavalon.wsdeckeditor.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.os.ResultReceiver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.joshuaavalon.android.view.ContentView;
import com.joshuaavalon.wsdeckeditor.BuildConfig;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.activity.MainActivity;
import com.joshuaavalon.wsdeckeditor.config.Version;
import com.joshuaavalon.wsdeckeditor.sdk.DownloadService;
import com.joshuaavalon.wsdeckeditor.util.WebUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

@ContentView(R.layout.fragment_update)
public class UpdateFragment extends BaseFragment implements Response.Listener<Integer>, Response.ErrorListener {
    private static final int CODE_CARD_DATABASE = 1;
    private static final int CODE_CARD_IMAGE = 2;
    @BindView(R.id.app_version_text_view)
    TextView applicationTextView;
    @BindView(R.id.db_version_text_view)
    TextView databaseTextView;
    @BindView(R.id.latest_app_version_text_view)
    TextView latestApplicationTextView;
    @BindView(R.id.latest_db_version_text_view)
    TextView latestDatabaseTextView;
    @BindView(R.id.update_database_button)
    Button updateDatabaseButton;
    @BindView(R.id.update_images_button)
    Button updateImagesButton;
    @BindView(R.id.update_app_button)
    Button updateApplicationButton;
    private DownloadReceiver receiver;
    private ProgressDialog progressDialog;

    @NonNull
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.bind(this, view);
        receiver = new DownloadReceiver(new Handler());
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        checkUpdate();
        applicationTextView.setText(BuildConfig.VERSION_NAME);
        getPreference().networkVersion(
                new Response.Listener<Version>() {
                    @Override
                    public void onResponse(Version response) {
                        final Version version = new Version(BuildConfig.VERSION_NAME);
                        if(response == null || response.compareTo(version) <= 0){
                            latestApplicationTextView.setVisibility(View.GONE);
                            return;
                        }
                        latestApplicationTextView.setVisibility(View.VISIBLE);
                        latestApplicationTextView.setText(response.get());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        latestApplicationTextView.setVisibility(View.GONE);
                    }
                });
        return view;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getString(R.string.nav_update);
    }

    private void checkUpdate() {
        getCardRepository().networkVersion(this, this);
        databaseTextView.setText(String.valueOf(getCardRepository().version()));
        if (activity() instanceof MainActivity)
            ((MainActivity) activity()).checkUpdate();
    }

    @OnClick(R.id.update_database_button)
    void updateDatabaseDialog() {
        new MaterialDialog.Builder(getContext())
                .title(R.string.dialog_update_database)
                .content(R.string.update_db_notice)
                .positiveText(R.string.dialog_update_button)
                .negativeText(R.string.dialog_cancel_button)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        toggleButton(false);
                        DownloadService.startDownloadCardDatabase(getContext(), receiver, CODE_CARD_DATABASE);
                        progressDialog.setTitle(R.string.dialog_update_database);
                    }
                })
                .show();
    }

    @OnClick(R.id.update_images_button)
    void updateImagesDialog() {
        new MaterialDialog.Builder(getContext())
                .title(R.string.dialog_update_database)
                .content(R.string.update_img_notice)
                .positiveText(R.string.dialog_update_button)
                .negativeText(R.string.dialog_cancel_button)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        toggleButton(false);
                        DownloadService.startDownloadImages(getContext(), receiver, CODE_CARD_IMAGE, false);
                        progressDialog.setTitle(R.string.dialog_update_images);
                    }
                })
                .show();
    }

    @OnClick(R.id.update_app_button)
    void updateApplication() {
        WebUtils.launchUrl(getContext(), getString(R.string.google_play_url));
    }

    private void toggleButton(final boolean enable) {
        updateDatabaseButton.setEnabled(enable);
        updateImagesButton.setEnabled(enable);
        updateApplicationButton.setEnabled(enable);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Timber.tag("UpdateFragment");
        Timber.w("VolleyError: " + error.getMessage());
        showMessage(R.string.msg_network_err);
    }

    @Override
    public void onResponse(Integer response) {
        if (response > getCardRepository().version()) {
            latestDatabaseTextView.setVisibility(View.VISIBLE);
            latestDatabaseTextView.setText(String.valueOf(response));
        } else
            latestDatabaseTextView.setVisibility(View.GONE);
    }

    private class DownloadReceiver extends ResultReceiver {
        public DownloadReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultData.containsKey(DownloadService.ARG_RESULT))
                showResult(resultCode, resultData);
            else
                updateProgress(resultData);
        }

        private void updateProgress(final Bundle resultData) {
            final int max = resultData.getInt(DownloadService.ARG_MAX_PROGRESS);
            if (max < 0) {
                progressDialog.setIndeterminate(true);
                return;
            }
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(max);
            final long progress = resultData.getInt(DownloadService.ARG_PROGRESS);
            progressDialog.setProgress((int) progress);
            if (!progressDialog.isShowing())
                progressDialog.show();
        }

        private void showResult(int resultCode, Bundle resultData) {
            if (resultData.getInt(DownloadService.ARG_RESULT, Activity.RESULT_CANCELED) ==
                    Activity.RESULT_CANCELED)
                showMessage(R.string.msg_network_err);
            else
                switch (resultCode) {
                    case CODE_CARD_DATABASE:
                        showMessage(R.string.msg_update_database);
                        checkUpdate();
                        break;
                    case CODE_CARD_IMAGE:
                        showMessage(R.string.msg_update_image);
                        break;
                }
            progressDialog.dismiss();
            toggleButton(true);
        }
    }
}
