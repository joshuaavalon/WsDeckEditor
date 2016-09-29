package com.joshuaavalon.wsdeckeditor.sdk.data.tool;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

public class VolleyLoader<T> extends Loader<VolleyLoader.Result<T>> {
    @NonNull
    private final Builder<T> builder;
    @NonNull
    private final RequestQueue queue;

    public VolleyLoader(@NonNull final Context context, @NonNull final RequestQueue queue,
                        @NonNull final Builder<T> builder) {
        super(context);
        this.queue = queue;
        this.builder = builder;
    }

    @Override
    protected void onForceLoad() {
        queue.add(builder.build(
                new Response.Listener<T>() {
                    @Override
                    public void onResponse(T response) {
                        deliverResult(new Result<>(response, null));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        deliverResult(new Result<T>(null, error));
                    }
                }
        ));
    }

    @Override
    public boolean cancelLoad() {
        return false;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    public static class Result<T> {
        @Nullable
        private final T result;
        @Nullable
        private final VolleyError volleyError;

        public Result(@Nullable final T result, @Nullable final VolleyError volleyError) {
            this.result = result;
            this.volleyError = volleyError;
        }

        @Nullable
        public T getResult() {
            return result;
        }

        @Nullable
        public VolleyError getVolleyError() {
            return volleyError;
        }
    }

    public interface Builder<T> {
        @NonNull
        Request<?> build(@NonNull final Response.Listener<T> listener,
                         @Nullable final Response.ErrorListener errorListener);
    }
}
