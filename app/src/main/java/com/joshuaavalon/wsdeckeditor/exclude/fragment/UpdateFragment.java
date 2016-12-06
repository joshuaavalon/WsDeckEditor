package com.joshuaavalon.wsdeckeditor.exclude.fragment;

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
import com.joshuaavalon.wsdeckeditor.exclude.MainActivity;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.sdk.DownloadService;

public class UpdateFragment extends BaseFragment implements Response.Listener<String>, Response.ErrorListener {
    private static final int CODE_CARD_DATABASE = 1;
    private static final int CODE_CARD_IMAGE = 2;
    private TextView latestTextView, currentTextView;
    private DownloadReceiver receiver;
    private ProgressDialog progressDialog;
    private Button updateDatabaseButton, updateImagesButton, downloadImagesButton;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_update, container, false);
        receiver = new DownloadReceiver(new Handler());
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        latestTextView = (TextView) view.findViewById(R.id.latest_text_view);
        currentTextView = (TextView) view.findViewById(R.id.current_text_view);
        updateDatabaseButton = (Button) view.findViewById(R.id.update_database_button);
        updateDatabaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDatabaseDialog();
            }
        });
        updateImagesButton = (Button) view.findViewById(R.id.update_images_button);
        updateImagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateImagesDialog();
            }
        });
        downloadImagesButton = (Button) view.findViewById(R.id.download_all_images_button);
        downloadImagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadImagesDialog();
            }
        });
        final UpdateHandler updateHandler = new UpdateHandler(getContext());
        updateHandler.getNetworkVersion(this, this);
        currentTextView.setText(String.valueOf(updateHandler.getDatabaseVersion()));
        return view;
    }

    private void updateDatabaseDialog() {
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

    private void updateImagesDialog() {
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

    private void downloadImagesDialog() {
        new MaterialDialog.Builder(getContext())
                .title(R.string.dialog_download_all_images)
                .content(R.string.download_img_notice)
                .positiveText(R.string.dialog_download_button)
                .negativeText(R.string.dialog_cancel_button)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        toggleButton(false);
                        DownloadService.startDownloadImages(getContext(), receiver, CODE_CARD_IMAGE, true);
                        progressDialog.setTitle(R.string.dialog_download_all_images);
                    }
                })
                .show();
    }

    @NonNull
    @Override
    public String getTitle() {
        return getString(R.string.nav_update);
    }

    private void toggleButton(final boolean enable) {
        updateDatabaseButton.setEnabled(enable);
        updateImagesButton.setEnabled(enable);
        downloadImagesButton.setEnabled(enable);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        showMessage(R.string.msg_network_err);
    }

    @Override
    public void onResponse(String response) {
        latestTextView.setText(response);
    }

    private class DownloadReceiver extends ResultReceiver {
        public DownloadReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (!resultData.containsKey(DownloadService.ARG_RESULT)) {
                final int max = resultData.getInt(DownloadService.ARG_MAX_PROGRESS);
                if (max == -1) {
                    progressDialog.setIndeterminate(true);
                    return;
                }
                progressDialog.setIndeterminate(false);
                progressDialog.setMax(max);
                final long progress = resultData.getInt(DownloadService.ARG_PROGRESS);
                progressDialog.setProgress((int) progress);
                if (!progressDialog.isShowing())
                    progressDialog.show();
                return;
            }
            if (resultData.getInt(DownloadService.ARG_RESULT, Activity.RESULT_CANCELED) == Activity.RESULT_CANCELED)
                showMessage(R.string.msg_network_err);
            else
                switch (resultCode) {
                    case CODE_CARD_DATABASE:
                        showMessage(R.string.msg_update_database);
                        final UpdateHandler updateHandler = new UpdateHandler(getContext());
                        currentTextView.setText(String.valueOf(updateHandler.getDatabaseVersion()));
                        ((MainActivity) getActivity()).showUpdateNotification(false);
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
