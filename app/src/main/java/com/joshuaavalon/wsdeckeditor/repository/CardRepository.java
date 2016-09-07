package com.joshuaavalon.wsdeckeditor.repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import com.google.common.base.Optional;
import com.joshuaavalon.fluentquery.Condition;
import com.joshuaavalon.fluentquery.Query;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.Utility;
import com.joshuaavalon.wsdeckeditor.WsApplication;
import com.joshuaavalon.wsdeckeditor.database.WsDatabaseHelper;
import com.joshuaavalon.wsdeckeditor.model.Card;
import com.joshuaavalon.wsdeckeditor.repository.model.CardFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CardRepository {
    public static final String SQL_CARD = "card";
    public static final String SQL_CARD_NAME = "Name";
    public static final String SQL_CARD_SERIAL = "Serial";
    public static final String SQL_CARD_RARITY = "Rarity";
    public static final String SQL_CARD_EXP = "Expansion";
    public static final String SQL_CARD_SIDE = "Side";
    public static final String SQL_CARD_COLOR = "Color";
    public static final String SQL_CARD_LEVEL = "Level";
    public static final String SQL_CARD_POWER = "Power";
    public static final String SQL_CARD_COST = "Cost";
    public static final String SQL_CARD_SOUL = "Soul";
    public static final String SQL_CARD_TYPE = "Type";
    public static final String SQL_CARD_FIRST_CHR = "FirstChara";
    public static final String SQL_CARD_SECOND_CHR = "SecondChara";
    public static final String SQL_CARD_TXT = "Text";
    public static final String SQL_CARD_FLAVOR = "Flavor";
    public static final String SQL_CARD_TRIGGER = "Trigger";
    public static final String SQL_CARD_IMAGE = "Image";
    public static final String SQL_VERSION = "version";
    public static final String SQL_VERSION_FIELD = "Version";

    public static List<Card> getCards(@NonNull final CardFilter filter) {
        final Query query = Query.select(getCols()).from(SQL_CARD);
        final Optional<Condition> conditionOptional = filter.getCondition();
        if (conditionOptional.isPresent())
            query.where(conditionOptional.get());
        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = query.commit(db);
        final List<Card> cards = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                cards.add(buildCard(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return cards;
    }

    @NonNull
    public static List<String> getAllImages() {
        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = Query.select(SQL_CARD_IMAGE).from(SQL_CARD).commit(db);
        List<String> images = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                images.add(cursor.getString(0));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return images;
    }

    @NonNull
    public static List<String> getExpansions() {
        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = Query.select(SQL_CARD_EXP).from(SQL_CARD).distinct().commit(db);
        List<String> expansions = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                expansions.add(cursor.getString(0));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return expansions;
    }

    @NonNull
    public static Optional<Card> getCardBySerial(@NonNull final String serial) {
        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = Query.select(getCols()).from(SQL_CARD)
                .where(Condition.property(SQL_CARD_SERIAL).equal(serial)).commit(db);
        Card card = null;
        if (cursor.moveToFirst()) {
            card = buildCard(cursor);
            cursor.close();
        }
        db.close();
        return Optional.fromNullable(card);
    }

    @NonNull
    public static Map<String, Card> getCardsBySerial(@NonNull final Collection<String> serials) {
        final Map<String, Card> cards = new HashMap<>();
        final SQLiteDatabase db = getReadableDatabase();
        Condition condition = null;
        for (String serial : serials) {
            if (condition == null)
                condition = Condition.property(SQL_CARD_SERIAL).equal(serial);
            else
                condition = condition.or(Condition.property(SQL_CARD_SERIAL).equal(serial));
        }
        if (condition == null) return cards;
        final Cursor cursor = Query.select(getCols()).from(SQL_CARD)
                .where(condition).commit(db);
        if (cursor.moveToFirst()) {
            do {
                final Card card = buildCard(cursor);
                cards.put(card.getSerial(), card);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return cards;
    }

    @NonNull
    private static String[] getCols() {
        return new String[]{
                SQL_CARD_NAME,
                SQL_CARD_SERIAL,
                SQL_CARD_RARITY,
                SQL_CARD_EXP,
                SQL_CARD_SIDE,
                SQL_CARD_COLOR,
                SQL_CARD_LEVEL,
                SQL_CARD_POWER,
                SQL_CARD_COST,
                SQL_CARD_SOUL,
                SQL_CARD_TYPE,
                SQL_CARD_FIRST_CHR,
                SQL_CARD_SECOND_CHR,
                SQL_CARD_TXT,
                SQL_CARD_FLAVOR,
                SQL_CARD_TRIGGER,
                SQL_CARD_IMAGE
        };
    }

    @NonNull
    private static Card buildCard(@NonNull final Cursor cursor) {
        final Card.Builder builder = new Card.Builder();
        builder.setName(cursor.getString(cursor.getColumnIndex(SQL_CARD_NAME)));
        builder.setSerial(cursor.getString(cursor.getColumnIndex(SQL_CARD_SERIAL)));
        builder.setRarity(cursor.getString(cursor.getColumnIndex(SQL_CARD_RARITY)));
        builder.setExpansion(cursor.getString(cursor.getColumnIndex(SQL_CARD_EXP)));
        builder.setSide(Card.Side.valueOf(cursor.getString(cursor.getColumnIndex(SQL_CARD_SIDE))));
        builder.setColor(Card.Color.valueOf(cursor.getString(cursor.getColumnIndex(SQL_CARD_COLOR))));
        builder.setLevel(cursor.getInt(cursor.getColumnIndex(SQL_CARD_LEVEL)));
        builder.setPower(cursor.getInt(cursor.getColumnIndex(SQL_CARD_POWER)));
        builder.setCost(cursor.getInt(cursor.getColumnIndex(SQL_CARD_COST)));
        builder.setSoul(cursor.getInt(cursor.getColumnIndex(SQL_CARD_SOUL)));
        builder.setType(Card.Type.valueOf(cursor.getString(cursor.getColumnIndex(SQL_CARD_TYPE))));
        builder.setAttribute1(cursor.getString(cursor.getColumnIndex(SQL_CARD_FIRST_CHR)));
        builder.setAttribute2(cursor.getString(cursor.getColumnIndex(SQL_CARD_SECOND_CHR)));
        builder.setText(cursor.getString(cursor.getColumnIndex(SQL_CARD_TXT)));
        builder.setFlavor(cursor.getString(cursor.getColumnIndex(SQL_CARD_FLAVOR)));
        builder.setTrigger(Card.Trigger.valueOf(cursor.getString(cursor.getColumnIndex(SQL_CARD_TRIGGER))));
        builder.setImage(Utility.getImageNameFromUrl(cursor.getString(cursor.getColumnIndex(SQL_CARD_IMAGE))));
        return builder.build();
    }

    public static int getVersion() {
        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = Query.select(SQL_VERSION_FIELD).from(SQL_VERSION).commit(db);
        int version = -1;
        if (cursor.moveToFirst())
            version = cursor.getInt(0);
        cursor.close();
        return version;
    }

    public static Bitmap getImage(String imageName, @NonNull final Card.Type type) {
        Bitmap bitmap;
        final Optional<Bitmap> bitmapOptional = NetworkRepository.getImage(imageName);
        if (bitmapOptional.isPresent())
            bitmap = bitmapOptional.get();
        else
            bitmap = BitmapFactory.decodeResource(WsApplication.getContext().getResources(),
                    type != Card.Type.Climax ? R.drawable.dc_w00_00 : R.drawable.dc_w00_000, null);
        return bitmap;
    }

    @NonNull
    private static SQLiteDatabase getReadableDatabase() {
        return new WsDatabaseHelper(WsApplication.getContext()).getReadableDatabase();
    }
}
