package com.joshuaavalon.wsdeckeditor.sdk.deck;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.List;

public class CacheDeckRepository implements IDeckRepository {
    @NonNull
    private final IDeckRepository deckRepository;
    private final LruCache<Long, Deck> deckLruCache;
    private final LruCache<Long, Integer> countLruCache;
    private List<DeckMeta> meta;

    public CacheDeckRepository(@NonNull final IDeckRepository deckRepository,
                               @NonNull final Builder builder) {
        this.deckRepository = deckRepository;
        deckLruCache = new LruCache<>(builder.deckCacheSize);
        countLruCache = new LruCache<>(builder.deckCacheSize);
        meta = null;
    }

    public static Builder builder(@NonNull final IDeckRepository deckRepository) {
        return new Builder(deckRepository);
    }

    @Override
    public void save(@NonNull Deck deck) {
        deckRepository.save(deck);
        deckLruCache.remove(deck.getId());
        countLruCache.remove(deck.getId());
        meta = null;
    }

    @Override
    public void save(@NonNull final DeckMeta meta) {
        if (meta.getId() == Deck.NO_ID) return;
        deckRepository.save(meta);
        deckLruCache.remove(meta.getId());
        this.meta = null;
    }

    @Override
    public void remove(@NonNull Deck deck) {
        deckRepository.remove(deck);
        deckLruCache.remove(deck.getId());
        countLruCache.remove(deck.getId());
        meta = null;
    }

    @Override
    public void remove(final long id) {
        deckRepository.remove(id);
        deckLruCache.remove(id);
        countLruCache.remove(id);
        meta = null;
    }

    @Override
    public void add(long id, @NonNull String serial, boolean requireNone) {
        deckRepository.add(id, serial, requireNone);
        deckLruCache.remove(id);
        countLruCache.remove(id);
    }

    @Override
    public void update(long id, @NonNull String serial, int count) {
        deckRepository.update(id, serial, count);
        deckLruCache.remove(id);
        countLruCache.remove(id);
    }

    @NonNull
    @Override
    public List<DeckMeta> meta() {
        return meta != null ? meta : deckRepository.meta();
    }

    @Nullable
    @Override
    public DeckMeta metaOf(final long id) {
        if (meta != null)
            return Iterables.find(meta(), new Predicate<DeckMeta>() {
                @Override
                public boolean apply(DeckMeta input) {
                    return input.getId() == id;
                }
            });
        return deckRepository.metaOf(id);
    }

    @Nullable
    @Override
    public Deck deck(long id) {
        Deck deck = deckLruCache.get(id);
        if (deck == null)
            deck = deckRepository.deck(id);
        if (deck != null)
            deckLruCache.put(id, deck);
        return deck;
    }

    @Override
    public int cardCount(long id) {
        Integer count = countLruCache.get(id);
        if (count == null) {
            count = deckRepository.cardCount(id);
            countLruCache.put(id, count);
        }
        return count;
    }

    public static class Builder {
        @NonNull
        private final IDeckRepository deckRepository;
        @IntRange(from = 1)
        private int deckCacheSize;

        private Builder(@NonNull IDeckRepository deckRepository) {
            this.deckRepository = deckRepository;
            deckCacheSize = 10;
        }

        public IDeckRepository build() {
            return new CacheDeckRepository(deckRepository, this);
        }

        public Builder setDeckCacheSize(int deckCacheSize) {
            this.deckCacheSize = deckCacheSize;
            return this;
        }
    }
}
