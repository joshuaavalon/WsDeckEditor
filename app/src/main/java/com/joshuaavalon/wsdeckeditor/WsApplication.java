package com.joshuaavalon.wsdeckeditor;

import android.app.Application;
import android.support.annotation.NonNull;

import com.joshuaavalon.wsdeckeditor.config.PreferenceRepository;
import com.joshuaavalon.wsdeckeditor.log.FirebaseTree;
import com.joshuaavalon.wsdeckeditor.sdk.card.CardFacade;
import com.joshuaavalon.wsdeckeditor.sdk.card.ICardRepository;
import com.joshuaavalon.wsdeckeditor.sdk.deck.DeckFacade;
import com.joshuaavalon.wsdeckeditor.sdk.deck.IDeckRepository;

import timber.log.Timber;

public class WsApplication extends Application {
    private ICardRepository cardRepository;
    private IDeckRepository deckRepository;
    private PreferenceRepository preference;

    @Override
    public void onCreate() {
        super.onCreate();
        cardRepository = CardFacade.Repository(this);
        deckRepository = DeckFacade.Repository(this, cardRepository);
        preference = PreferenceRepository.fromDefault(this);
        if (BuildConfig.DEBUG)
            Timber.plant(new Timber.DebugTree());
        else
            Timber.plant(new FirebaseTree());
        if(preference.getAppVerion() >= BuildConfig.VERSION_CODE) return;
        cardRepository.reset();
        preference.setAppVerion(BuildConfig.VERSION_CODE);
    }

    @NonNull
    public synchronized ICardRepository getCardRepository() {
        return cardRepository;
    }

    @NonNull
    public synchronized IDeckRepository getDeckRepository() {
        return deckRepository;
    }

    @NonNull
    public synchronized PreferenceRepository getPreference() {
        return preference;
    }
}
