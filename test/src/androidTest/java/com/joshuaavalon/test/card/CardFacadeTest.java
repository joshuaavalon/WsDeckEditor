package com.joshuaavalon.test.card;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;

import com.joshuaavalon.test.R;
import com.joshuaavalon.wsdeckeditor.sdk.card.CardFacade;
import com.joshuaavalon.wsdeckeditor.sdk.card.ICardRepository;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class CardFacadeTest {
    private static ICardRepository repository;

    @BeforeClass
    public static void beforeClass() {
        final Context context = InstrumentationRegistry.getTargetContext();
        repository = CardFacade.Repository(context);
        repository.updateDatabase(context.getResources().openRawResource(R.raw.wsdb));
    }

    @Test
    @MediumTest
    public void versionTest() {
        Assert.assertEquals(1, repository.getVersion());
    }

    @After
    public void after() {
        InstrumentationRegistry.getTargetContext().deleteDatabase(CardFacade.DATABASE_NAME);
    }
}
