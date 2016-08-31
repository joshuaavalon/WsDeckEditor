package com.joshuaavalon.wsdeckeditor.activity;

import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.joshuaavalon.wsdeckeditor.R;

public abstract class BaseActivity extends AppCompatActivity {

    public void showMessage(@StringRes final int resId) {
        final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        if (coordinatorLayout != null)
            Snackbar.make(coordinatorLayout, resId, Snackbar.LENGTH_LONG).show();
        else
            Toast.makeText(this, resId, Toast.LENGTH_LONG).show();
    }
}
