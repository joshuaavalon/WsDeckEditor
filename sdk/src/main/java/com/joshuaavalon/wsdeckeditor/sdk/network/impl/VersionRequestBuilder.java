package com.joshuaavalon.wsdeckeditor.sdk.network.impl;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.joshuaavalon.wsdeckeditor.sdk.network.VolleyLoader;

public class VersionRequestBuilder implements VolleyLoader.Builder<Integer> {
    @NonNull
    private final String url;

    public VersionRequestBuilder(@NonNull final String url) {
        this.url = url;
    }

    @NonNull
    @Override
    public Request<String> build(@NonNull final Response.Listener<Integer> listener,
                                 @Nullable final Response.ErrorListener errorListener) {
        return new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                listener.onResponse(Integer.valueOf(response));
            }
        }, errorListener);
    }
}
