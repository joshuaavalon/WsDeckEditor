package com.joshuaavalon.wsdeckeditor.sdk.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.joshuaavalon.wsdeckeditor.sdk.data.CardDatabase;
import com.joshuaavalon.wsdeckeditor.sdk.data.ConfigConstant;

public class UpdateHandler {
    @NonNull
    private final Context context;

    public UpdateHandler(@NonNull final Context context) {
        this.context = context.getApplicationContext();
    }

    public void getNetworkVersion(@NonNull final Response.Listener<String> listener,
                                  @Nullable final Response.ErrorListener errorListener) {
        final RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(new StringRequest(Request.Method.GET, ConfigConstant.URL_VERSION, listener, errorListener));
    }

    public int getDatabaseVersion() {
        return new CardDatabase(context).getVersion();
    }

    public void getUpdateNeed(@NonNull final Response.Listener<Boolean> listener,
                              @Nullable final Response.ErrorListener errorListener) {
        final int databaseVersion = getDatabaseVersion();
        getNetworkVersion(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                listener.onResponse(Integer.valueOf(response) > databaseVersion);
            }
        }, errorListener);
    }
}
