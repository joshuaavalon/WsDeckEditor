package com.joshuaavalon.wsdeckeditor.activity;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import com.joshuaavalon.wsdeckeditor.R;

public abstract class BaseActivity extends AppCompatActivity {
    public void showMessage(@StringRes final int resId) {
        showMessage(resId, Snackbar.LENGTH_LONG);
    }

    public void showMessage(@NonNull final String message) {
        showMessage(message, Snackbar.LENGTH_LONG);
    }

    public void showMessage(@StringRes final int resId, final int duration) {
        final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        if (coordinatorLayout != null)
            Snackbar.make(coordinatorLayout, resId, duration).show();
    }

    public void showMessage(@NonNull final String message, final int duration) {
        final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        if (coordinatorLayout != null)
            Snackbar.make(coordinatorLayout, message, duration).show();
    }
}
