package com.joshuaavalon.wsdeckeditor;

import android.app.Application;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;

import com.joshuaavalon.wsdeckeditor.config.PreferenceRepository;
import com.joshuaavalon.wsdeckeditor.sdk.card.CardFacade;
import com.joshuaavalon.wsdeckeditor.sdk.card.ICardRepository;
import com.joshuaavalon.wsdeckeditor.sdk.deck.DeckFacade;
import com.joshuaavalon.wsdeckeditor.sdk.deck.IDeckRepository;

import timber.log.Timber;

public class WsApplication extends Application {
    @Nullable
    private LruCache<String, Bitmap> bitmapLruCache;
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
    public LruCache<String, Bitmap> getBitmapCache() {
        if (bitmapLruCache == null) {
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            final int cacheSize = maxMemory / 8;
            bitmapLruCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    return bitmap.getByteCount() / 1024;
                }
            };
        }
        return bitmapLruCache;
    }

    @NonNull
    public ICardRepository getCardRepository() {
        if (cardRepository == null)
            cardRepository = CardFacade.Repository(this);
        return cardRepository;
    }

    @NonNull
    public IDeckRepository getDeckRepository() {
        if (deckRepository == null)
            deckRepository = DeckFacade.Repository(this, getCardRepository());
        return deckRepository;
    }

    @NonNull
    public PreferenceRepository getPreference() {
        if (preference == null)
            preference = PreferenceRepository.fromDefault(this);
        return preference;
    }
}
