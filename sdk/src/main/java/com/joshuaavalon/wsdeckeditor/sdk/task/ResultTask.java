package com.joshuaavalon.wsdeckeditor.sdk.task;


import android.os.AsyncTask;
import android.support.annotation.Nullable;

public abstract class ResultTask<T, V> extends AsyncTask<T, Void, V> {
    @Nullable
    private final CallBack<V> callBack;

    protected ResultTask(@Nullable final CallBack<V> callBack) {
        this.callBack = callBack;
    }

    @Override
    protected void onPostExecute(V result) {
        super.onPostExecute(result);
        if (callBack != null)
            callBack.onResult(result);
    }

    public interface CallBack<T> {
        void onResult(T result);
    }
}
