package com.joshuaavalon.wsdeckeditor.database;

import android.support.annotation.NonNull;

import com.orm.SugarRecord;

import java.util.List;

public class DeckDao extends SugarRecord {
    @NonNull
    private String name;

    public DeckDao(@NonNull final String name) {
        this.name = name;
    }

    @NonNull
    public List<DeckRecord> getRecords() {
        return DeckRecord.find(DeckRecord.class, "DECK = ?", String.valueOf(getId()));
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull final String name) {
        this.name = name;
    }
}
