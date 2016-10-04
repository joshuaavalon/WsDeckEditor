package com.joshuaavalon.wsdeckeditor;


import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.os.ResultReceiver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.joshuaavalon.wsdeckeditor.sdk.data.CardRepository;
import com.joshuaavalon.wsdeckeditor.sdk.data.DownloadService;
import com.joshuaavalon.wsdeckeditor.sdk.data.tool.VolleyLoader;

public class UpdateFragment extends BaseFragment {
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
        latestTextView.setText(getString(R.string.format_latest_version, "?"));
        currentTextView = (TextView) view.findViewById(R.id.current_text_view);
        currentTextView.setText(getString(R.string.format_current_version, "?"));
        updateDatabaseButton = (Button) view.findViewById(R.id.update_database_button);
        updateDatabaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleButton(false);
                DownloadService.startDownloadCardDatabase(getContext(), receiver, CODE_CARD_DATABASE);
                progressDialog.setTitle(R.string.dialog_update_database);
                progressDialog.show();
            }
        });
        updateImagesButton = (Button) view.findViewById(R.id.update_images_button);
        updateImagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleButton(false);
                DownloadService.startDownloadImages(getContext(), receiver, CODE_CARD_IMAGE, false);
                progressDialog.setTitle(R.string.dialog_update_images);
                progressDialog.show();
            }
        });
        downloadImagesButton = (Button) view.findViewById(R.id.download_all_images_button);
        downloadImagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleButton(false);
                DownloadService.startDownloadImages(getContext(), receiver, CODE_CARD_IMAGE, true);
                progressDialog.setTitle(R.string.dialog_download_all_images);
                progressDialog.show();
            }
        });
        getActivity().getSupportLoaderManager().initLoader(LoaderId.VersionLoader, null, new VersionLoaderCallBack());
        getActivity().getSupportLoaderManager().restartLoader(LoaderId.NetworkVersionLoader, null, new NetworkVersionLoaderCallBack());
        return view;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getString(R.string.nav_update);
    }

    private class VersionLoaderCallBack implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return CardRepository.newVersionLoader(getContext());
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            currentTextView.setText(getString(R.string.format_current_version, String.valueOf(CardRepository.toVersion(data))));
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }

    private class NetworkVersionLoaderCallBack implements LoaderManager.LoaderCallbacks<VolleyLoader.Result<Integer>> {

        @Override
        public Loader<VolleyLoader.Result<Integer>> onCreateLoader(int id, Bundle args) {
            return CardRepository.newNetworkVersionLoader(getContext());
        }

        @Override
        public void onLoadFinished(Loader<VolleyLoader.Result<Integer>> loader, VolleyLoader.Result<Integer> data) {
            if (data.getResult() != null) {
                latestTextView.setText(getString(R.string.format_latest_version, String.valueOf(data.getResult())));
            } else {
                Snackbar.make(((SnackBarSupport) getActivity()).getCoordinatorLayout(), R.string.msg_network_err, Snackbar.LENGTH_LONG);
            }
        }

        @Override
        public void onLoaderReset(Loader<VolleyLoader.Result<Integer>> loader) {

        }
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
                return;
            }
            if (resultData.getInt(DownloadService.ARG_RESULT, Activity.RESULT_CANCELED) == Activity.RESULT_CANCELED)
                Snackbar.make(((SnackBarSupport) getActivity()).getCoordinatorLayout(),
                        R.string.msg_network_err, Snackbar.LENGTH_LONG).show();
            else
                switch (resultCode) {
                    case CODE_CARD_DATABASE:
                        Snackbar.make(((SnackBarSupport) getActivity()).getCoordinatorLayout(),
                                R.string.msg_update_database, Snackbar.LENGTH_LONG).show();
                        getActivity().getSupportLoaderManager().restartLoader(LoaderId.VersionLoader, null, new VersionLoaderCallBack());
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

    private void toggleButton(final boolean enable) {
        updateDatabaseButton.setEnabled(enable);
        updateImagesButton.setEnabled(enable);
        downloadImagesButton.setEnabled(enable);
    }
}
