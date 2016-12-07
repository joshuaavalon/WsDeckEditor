package com.joshuaavalon.test.deck;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;

import com.joshuaavalon.wsdeckeditor.sdk.card.Card;
import com.joshuaavalon.wsdeckeditor.sdk.card.CardFacade;
import com.joshuaavalon.wsdeckeditor.sdk.card.ICardRepository;
import com.joshuaavalon.wsdeckeditor.sdk.deck.Deck;
import com.joshuaavalon.wsdeckeditor.sdk.deck.DeckFacade;
import com.joshuaavalon.wsdeckeditor.sdk.deck.DeckMeta;
import com.joshuaavalon.wsdeckeditor.sdk.deck.IDeckRepository;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class DeckFacadeTest {
    private static IDeckRepository deckRepository;
    private static ICardRepository cardRepository;

    @BeforeClass
    public static void beforeClass() {
        final Context context = InstrumentationRegistry.getTargetContext();
        cardRepository = CardFacade.Repository(context);
        deckRepository = DeckFacade.Repository(context, cardRepository);
    }

    @Test
    @MediumTest
    public void saveTest() {
        final Deck deck = new Deck();
        deck.setName("a");
        deck.setCover("DC/W01-001");
        final Card card = cardRepository.find("DC/W01-001");
        Assert.assertNotNull(card);
        deck.setCardCount(card, 3);
        Assert.assertEquals(0, deckRepository.meta().size());
        deckRepository.save(deck);
        final List<DeckMeta> deckMetas = deckRepository.meta();
        Assert.assertEquals(1, deckMetas.size());
        final DeckMeta meta = deckMetas.get(0);
        Assert.assertEquals("a", meta.getName());
        Assert.assertEquals("DC/W01-001", meta.getCover());
        Assert.assertNotEquals(Deck.NO_ID, meta.getId());
        final Deck deck1 = deckRepository.deck(meta.getId());
        Assert.assertNotNull(deck1);
        Assert.assertEquals(3, deck1.getCardList().count(card));
        deck1.setCardCount(card, 1);
        deckRepository.save(deck1);
        final Deck deck2 = deckRepository.deck(meta.getId());
        Assert.assertNotNull(deck2);
        Assert.assertEquals(1, deck2.getCardList().count(card));
    }

    @Test
    @MediumTest
    public void removeTest() {
        final Deck deck = new Deck();
        deck.setName("a");
        deck.setCover("DC/W01-001");
        final Card card = cardRepository.find("DC/W01-001");
        Assert.assertNotNull(card);
        deck.setCardCount(card, 3);
        Assert.assertEquals(0, deckRepository.meta().size());
        deckRepository.save(deck);
        Assert.assertEquals(1, deckRepository.meta().size());
        deckRepository.remove(deck);
        Assert.assertEquals(0, deckRepository.meta().size());
    }

    @Test
    @MediumTest
    public void addCardTest() {
        Deck deck = new Deck();
        deck.setName("a");
        Assert.assertEquals(0, deckRepository.meta().size());
        deckRepository.save(deck);
        Assert.assertEquals(1, deckRepository.meta().size());
        deckRepository.add(deck.getId(), "DC/W01-001", true);
        deck = deckRepository.deck(deck.getId());
        Assert.assertNotNull(deck);
        final Card card = cardRepository.find("DC/W01-001");
        Assert.assertEquals(1, deck.getCardList().count(card));
        deckRepository.add(deck.getId(), "DC/W01-001", true);
        deck = deckRepository.deck(deck.getId());
        Assert.assertNotNull(deck);
        Assert.assertEquals(1, deck.getCardList().count(card));
        deckRepository.add(deck.getId(), "DC/W01-001", false);
        deck = deckRepository.deck(deck.getId());
        Assert.assertNotNull(deck);
        Assert.assertEquals(2, deck.getCardList().count(card));
        deckRepository.add(deck.getId(), "DC/W01-001", true);
        deck = deckRepository.deck(deck.getId());
        Assert.assertNotNull(deck);
        Assert.assertEquals(2, deck.getCardList().count(card));
    }

    @Test
    @MediumTest
    public void updateCardTest() {
        Deck deck = new Deck();
        deck.setName("a");
        Assert.assertEquals(0, deckRepository.meta().size());
        deckRepository.save(deck);
        Assert.assertEquals(1, deckRepository.meta().size());
        deckRepository.update(deck.getId(), "DC/W01-001", 1);
        deck = deckRepository.deck(deck.getId());
        Assert.assertNotNull(deck);
        final Card card = cardRepository.find("DC/W01-001");
        Assert.assertEquals(1, deck.getCardList().count(card));
        deckRepository.update(deck.getId(), "DC/W01-001", 0);
        deck = deckRepository.deck(deck.getId());
        Assert.assertNotNull(deck);
        Assert.assertEquals(0, deck.getCardList().count(card));
        deckRepository.update(deck.getId(), "DC/W01-001", 3);
        deck = deckRepository.deck(deck.getId());
        Assert.assertNotNull(deck);
        Assert.assertEquals(3, deck.getCardList().count(card));
    }

    @After
    public void after() {
        final Context context = InstrumentationRegistry.getTargetContext();
        context.deleteDatabase(DeckFacade.DATABASE_NAME);
        context.deleteDatabase(CardFacade.DATABASE_NAME);
    }
}
