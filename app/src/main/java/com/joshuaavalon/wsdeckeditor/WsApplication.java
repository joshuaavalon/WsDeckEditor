package com.joshuaavalon.wsdeckeditor;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.joshuaavalon.wsdeckeditor.config.PreferenceRepository;
import com.joshuaavalon.wsdeckeditor.sdk.card.CardFacade;
import com.joshuaavalon.wsdeckeditor.sdk.card.ICardRepository;
import com.joshuaavalon.wsdeckeditor.sdk.deck.DeckFacade;
import com.joshuaavalon.wsdeckeditor.sdk.deck.IDeckRepository;

import timber.log.Timber;

public class WsApplication extends Application {
    @Nullable
    private ICardRepository cardRepository;
    @Nullable
    private IDeckRepository deckRepository;
    @Nullable
    private PreferenceRepository preference;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG)
            Timber.plant(new Timber.DebugTree());
    }

    @NonNull
    public synchronized ICardRepository getCardRepository() {
        if (cardRepository == null)
            cardRepository = CardFacade.Repository(this);
        return cardRepository;
    }

    @NonNull
    public synchronized IDeckRepository getDeckRepository() {
        if (deckRepository == null)
            deckRepository = DeckFacade.Repository(this, getCardRepository());
        return deckRepository;
    }

    @NonNull
    public synchronized PreferenceRepository getPreference() {
        if (preference == null)
            preference = PreferenceRepository.fromDefault(this);
        return preference;
    }
}
