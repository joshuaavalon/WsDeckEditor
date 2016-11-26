package com.joshuaavalon.wsdeckeditor.sdk.task;

import android.support.annotation.Nullable;

import com.joshuaavalon.wsdeckeditor.sdk.data.CardDatabase;

import java.util.List;

public class ExpansionLoadTask extends StringLoadTask {
    public ExpansionLoadTask(@Nullable final CallBack<List<String>> callBack) {
        super(CardDatabase.Table.Card, CardDatabase.Field.Expansion, callBack);
    }
}
