package com.joshuaavalon.wsdeckeditor.model;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.primitives.Ints;
import com.joshuaavalon.wsdeckeditor.WsApplication;
import com.joshuaavalon.wsdeckeditor.repository.CardRepository;

import java.util.ArrayList;
import java.util.List;

public class DeckUtils {
    private static final String COUNT_SEPARATOR = ":";
    private static final String CARD_SEPARATOR = ";";
    private static final String DECK_NAME_SEPARATOR = "|";
    private static final String SCHEME = WsApplication.QR_SCHEME + "://";
    private static final int DECK_LIMIT = 50;

    public static CharSequence getStatusLabel(@NonNull final Deck deck) {
        final int count = getCount(deck);
        final String countLabel = count > 99 ? "99+" : String.valueOf(count);
        final SpannableString countLabelSpan = new SpannableString(countLabel);
        if (count != DECK_LIMIT)
            countLabelSpan.setSpan(new ForegroundColorSpan(
                    (count > DECK_LIMIT) ? Color.RED : Color.GRAY), 0, countLabel.length(), 0);
        final SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(countLabelSpan);
        builder.append(" / ");
        builder.append(String.valueOf(DECK_LIMIT));
        return builder;
    }

    public static int getCount(@NonNull final Deck deck) {
        return deck.getList().size();
    }

    @NonNull
    public static Multiset<Card.Color> getColorCount(@NonNull final Deck deck) {
        final Multiset<Card.Color> colorCount = HashMultiset.create();
        for (Multiset.Entry<Card> entry : deck.getList().entrySet()) {
            colorCount.add(entry.getElement().getColor(), entry.getCount());
        }
        return colorCount;
    }

    @NonNull
    public static String encodeDeck(@NonNull final Deck deck) {
        final Multiset<Card> deckList = deck.getList();
        final List<String> deckStrings = new ArrayList<>();
        for (Card card : deckList.elementSet()) {
            deckStrings.add(card.getSerial() + COUNT_SEPARATOR + deckList.count(card));
        }
        final String deckString = deck.getName() +
                DECK_NAME_SEPARATOR +
                Joiner.on(CARD_SEPARATOR).join(deckStrings);
        return SCHEME + Base64.encodeToString(deckString.getBytes(Charsets.UTF_8), Base64.DEFAULT);
    }

    @NonNull
    public static Optional<Deck> decodeDeck(@NonNull final String encodeString) {
        final String deckString = new String(Base64.decode(encodeString, Base64.DEFAULT), Charsets.UTF_8);
        final String[] info = deckString.split("\\|", -1);
        if (info.length != 2) return Optional.absent();
        final Deck deck = new Deck();
        deck.setName(info[0]);
        final String[] cardEntries = info[1].split(CARD_SEPARATOR);
        final Multiset<String> serials = HashMultiset.create();
        for (String entry : cardEntries) {
            final String[] cardDetail = entry.split(COUNT_SEPARATOR);
            if (cardDetail.length != 2) continue;
            final Integer count = Ints.tryParse(cardDetail[1]);
            if (count == null) continue;
            serials.setCount(cardDetail[0], count);
        }
        for (String serial : CardRepository.getCardsBySerial(serials.elementSet()).keySet()) {
            deck.setCount(serial, serials.count(serial));
        }
        return Optional.of(deck);
    }
}
