package com.joshuaavalon.wsdeckeditor.activity;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import com.joshuaavalon.wsdeckeditor.R;

/**
 * Provide basic interface to show message.
 * It requires {@link CoordinatorLayout}.
 */
public abstract class BaseActivity extends AppCompatActivity {
    /**
     * Show a message.
     *
     * @param resId String resource id of the message to be shown.
     */
    public void showMessage(@StringRes final int resId) {
        showMessage(resId, Snackbar.LENGTH_LONG);
    }

    /**
     * Show a message.
     *
     * @param message Message to be shown.
     */
    public void showMessage(@NonNull final String message) {
        showMessage(message, Snackbar.LENGTH_LONG);
    }

    /**
     * Show a message.
     *
     * @param resId    String resource id of the message to be shown.
     * @param duration Duration of the message shown. Please refer to {@link Snackbar}
     */
    public void showMessage(@StringRes final int resId, final int duration) {
        final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        Snackbar.make(coordinatorLayout, resId, duration).show();
    }

    /**
     * Show a message.
     *
     * @param message  Message to be shown.
     * @param duration Duration of the message shown. Please refer to {@link Snackbar}
     */
    public void showMessage(@NonNull final String message, final int duration) {
        final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        Snackbar.make(coordinatorLayout, message, duration).show();
    }
}
