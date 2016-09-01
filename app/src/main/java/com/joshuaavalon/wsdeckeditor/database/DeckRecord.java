package com.joshuaavalon.wsdeckeditor.database;

import android.support.annotation.NonNull;

import com.orm.SugarRecord;

public class DeckRecord extends SugarRecord {
    @NonNull
    private String serial;
    private int count;
    private DeckDao deck;

    public DeckRecord() {
        serial = "";
        count = 0;
    }

    public DeckRecord(@NonNull final String serial, final int count, @NonNull final DeckDao deck) {
        this.serial = serial;
        this.count = count;
        this.deck = deck;
    }

    @NonNull
    public String getSerial() {
        return serial;
    }

    public void setSerial(@NonNull final String serial) {
        this.serial = serial;
    }

    public int getCount() {
        return count;
    }

    public void setCount(final int count) {
        this.count = count;
    }

    @NonNull
    public DeckDao getDeck() {
        return deck;
    }

    public void setDeck(@NonNull final DeckDao deck) {
        this.deck = deck;
    }
}
