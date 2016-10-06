package com.joshuaavalon.wsdeckeditor.sdk.task;


import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.webkit.URLUtil;

import com.joshuaavalon.wsdeckeditor.sdk.Card;
import com.joshuaavalon.wsdeckeditor.sdk.data.CardDatabase;

import java.util.List;

public abstract class CardLoadTask extends ResultTask<Context, List<Card>> {

    protected CardLoadTask(@Nullable final CallBack<List<Card>> callBack) {
        super(callBack);
    }

    @NonNull
    protected static Card buildCard(@NonNull final Cursor cursor) {
        final Card.Builder builder = new Card.Builder();
        builder.setName(cursor.getString(cursor.getColumnIndexOrThrow(CardDatabase.Field.Name)));
        builder.setSerial(cursor.getString(cursor.getColumnIndexOrThrow(CardDatabase.Field.Serial)));
        builder.setRarity(cursor.getString(cursor.getColumnIndexOrThrow(CardDatabase.Field.Rarity)));
        builder.setExpansion(cursor.getString(cursor.getColumnIndexOrThrow(CardDatabase.Field.Expansion)));
        builder.setSide(Card.Side.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(CardDatabase.Field.Side))));
        builder.setColor(Card.Color.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(CardDatabase.Field.Color))));
        builder.setLevel(cursor.getInt(cursor.getColumnIndexOrThrow(CardDatabase.Field.Level)));
        builder.setPower(cursor.getInt(cursor.getColumnIndexOrThrow(CardDatabase.Field.Power)));
        builder.setCost(cursor.getInt(cursor.getColumnIndexOrThrow(CardDatabase.Field.Cost)));
        builder.setSoul(cursor.getInt(cursor.getColumnIndexOrThrow(CardDatabase.Field.Soul)));
        builder.setType(Card.Type.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(CardDatabase.Field.Type))));
        builder.setAttribute1(cursor.getString(cursor.getColumnIndexOrThrow(CardDatabase.Field.FirstChara)));
        builder.setAttribute2(cursor.getString(cursor.getColumnIndexOrThrow(CardDatabase.Field.SecondChara)));
        builder.setText(cursor.getString(cursor.getColumnIndexOrThrow(CardDatabase.Field.Text)));
        builder.setFlavor(cursor.getString(cursor.getColumnIndexOrThrow(CardDatabase.Field.Flavor)));
        builder.setTrigger(Card.Trigger.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(CardDatabase.Field.Trigger))));
        builder.setImage(URLUtil.guessFileName(cursor.getString(cursor.getColumnIndexOrThrow(CardDatabase.Field.Image))
                , null, null));
        return builder.build();
    }
}
