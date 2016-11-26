package com.joshuaavalon.wsdeckeditor.sdk.task;

import android.support.annotation.Nullable;

import com.joshuaavalon.wsdeckeditor.sdk.data.CardDatabase;

import java.util.List;

public class ImageUrlLoadTask extends StringLoadTask {
    public ImageUrlLoadTask(@Nullable final CallBack<List<String>> callBack) {
        super(CardDatabase.Table.Card, CardDatabase.Field.Image, callBack);
    }
}