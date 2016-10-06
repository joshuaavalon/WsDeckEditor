package com.joshuaavalon.wsdeckeditor.fragment;


import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.os.ResultReceiver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.SnackBarSupport;
import com.joshuaavalon.wsdeckeditor.sdk.data.CardDatabase;
import com.joshuaavalon.wsdeckeditor.sdk.data.ConfigConstant;
import com.joshuaavalon.wsdeckeditor.sdk.data.DownloadService;

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
                toggleButton(false);
                DownloadService.startDownloadCardDatabase(getContext(), receiver, CODE_CARD_DATABASE);
                progressDialog.setTitle(R.string.dialog_update_database);
            }
        });
        updateImagesButton = (Button) view.findViewById(R.id.update_images_button);
        updateImagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleButton(false);
                DownloadService.startDownloadImages(getContext(), receiver, CODE_CARD_IMAGE, false);
                progressDialog.setTitle(R.string.dialog_update_images);
            }
        });
        downloadImagesButton = (Button) view.findViewById(R.id.download_all_images_button);
        downloadImagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleButton(false);
                DownloadService.startDownloadImages(getContext(), receiver, CODE_CARD_IMAGE, true);
                progressDialog.setTitle(R.string.dialog_download_all_images);
            }
        });
        updateCurrentVersion();
        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(new StringRequest(Request.Method.GET, ConfigConstant.URL_VERSION, this, this));
        return view;
    }

    private void updateCurrentVersion() {
        currentTextView.setText(String.valueOf(new CardDatabase(getContext()).getVersion()));
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
        showNetworkError();
    }

    @Override
    public void onResponse(String response) {
        latestTextView.setText(response);
    }

    private void showNetworkError() {
        final Activity activity = getActivity();
        if (activity == null || !(activity instanceof SnackBarSupport)) return;
        final SnackBarSupport snackBarSupport = (SnackBarSupport) getActivity();
        Snackbar.make(snackBarSupport.getCoordinatorLayout(), R.string.msg_network_err, Snackbar.LENGTH_LONG).show();
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
                showNetworkError();
            else
                switch (resultCode) {
                    case CODE_CARD_DATABASE:
                        Snackbar.make(((SnackBarSupport) getActivity()).getCoordinatorLayout(),
                                R.string.msg_update_database, Snackbar.LENGTH_LONG).show();
                        updateCurrentVersion();
                        break;
                    case CODE_CARD_IMAGE:
                        Snackbar.make(((SnackBarSupport) getActivity()).getCoordinatorLayout(),
                                R.string.msg_update_image, Snackbar.LENGTH_LONG).show();
                        break;
                }
            progressDialog.dismiss();
            toggleButton(true);
        }
    }
}
