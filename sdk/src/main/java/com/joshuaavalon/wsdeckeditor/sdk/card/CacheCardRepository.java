package com.joshuaavalon.wsdeckeditor.sdk.card;

import android.graphics.Bitmap;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.common.base.Objects;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

class CacheCardRepository implements ICardRepository {
    @NonNull
    private final ICardRepository cardRepository;
    @NonNull
    private final LruCache<String, Bitmap> bitmapLruCache;
    @NonNull
    private final LruCache<Integer, Bitmap> thumbnailLruCache;
    @NonNull
    private final LruCache<String, Card> cardLruCache;
    @NonNull
    private final LruCache<Integer, List<String>> filterLruCache;
    @IntRange(from = 0)
    private final int cacheTime; // In minutes
    private int version;
    private int networkVersion;
    @Nullable
    private List<String> expansions;
    @Nullable
    private Calendar lastUpdated;

    public CacheCardRepository(@NonNull final ICardRepository cardRepository,
                               @NonNull final Builder builder) {
        this.cardRepository = cardRepository;
        this.bitmapLruCache = builder.bitmapLruCache;
        this.thumbnailLruCache = builder.thumbnailLruCache;
        version = -1;
        networkVersion = -1;
        cardLruCache = new LruCache<>(builder.cardCacheSize);
        filterLruCache = new LruCache<>(builder.filterCacheSize);
        cacheTime = builder.cacheTime;
        lastUpdated = null;
    }

    public static Builder builder(@NonNull final ICardRepository cardRepository) {
        return new Builder(cardRepository);
    }

    @Override
    public int version() {
        return version > 0 ? version : cardRepository.version();
    }

    @Nullable
    @Override
    public Card random() {
        return cardRepository.random();
    }

    @Nullable
    @Override
    public Card find(@NonNull final String serial) {
        Card card = cardLruCache.get(serial);
        if (card != null) return card;
        card = cardRepository.find(serial);
        if (card != null)
            cardLruCache.put(serial, card);
        return card;
    }

    @NonNull
    @Override
    public List<Card> findAll(@NonNull final List<String> serials) {
        final List<String> missingSerials = new ArrayList<>();
        final List<Card> result = new ArrayList<>();
        for (String serial : serials) {
            final Card card = cardLruCache.get(serial);
            if (card == null)
                missingSerials.add(serial);
            else {
                cardLruCache.put(serial, card);
                result.add(card);
            }
        }
        if (missingSerials.size() > 0)
            result.addAll(cardRepository.findAll(missingSerials));
        return result;
    }

    @NonNull
    @Override
    public Bitmap imageOf(@Nullable final Card card) {
        Bitmap bitmap = null;
        if (card != null)
            bitmap = bitmapLruCache.get(card.getImage());
        if (bitmap == null)
            bitmap = cardRepository.imageOf(card);
        if (card != null)
            bitmapLruCache.put(card.getImage(), bitmap);
        return bitmap;
    }

    @NonNull
    @Override
    public Bitmap thumbnailOf(@NonNull Bitmap bitmap) {
        final int key = bitmap.hashCode();
        Bitmap thumbnail = thumbnailLruCache.get(key);
        if (thumbnail == null)
            thumbnail = cardRepository.thumbnailOf(bitmap);
        thumbnailLruCache.put(key, thumbnail);
        return thumbnail;
    }

    @Override
    public void updateDatabase(@NonNull final InputStream in) {
        invalidate();
        cardRepository.updateDatabase(in);
    }

    @NonNull
    @Override
    public List<Card> findAll(@NonNull final Filter filter, final int limit, final int offset) {
        final int hashKey = Objects.hashCode(filter, limit, offset);
        final List<String> serials = filterLruCache.get(hashKey);
        List<Card> result;
        if (serials == null)
            result = cardRepository.findAll(filter, limit, offset);
        else
            result = findAll(serials);
        return result;
    }

    @NonNull
    @Override
    public List<String> imageUrls() {
        return cardRepository.imageUrls();
    }

    @NonNull
    @Override
    public List<String> expansions() {
        if (expansions == null)
            expansions = cardRepository.expansions();
        return expansions;
    }

    @Override
    public List<String> keywords(@NonNull String query, int limit) {
        return cardRepository.keywords(query, limit);
    }

    @Override
    public void networkVersion(@NonNull final Response.Listener<Integer> listener,
                               @Nullable final Response.ErrorListener errorListener) {
        final Calendar now = Calendar.getInstance();
        final boolean needUpdate = lastUpdated == null ||
                TimeUnit.MILLISECONDS.toMinutes(now.getTimeInMillis() - lastUpdated.getTimeInMillis()) > cacheTime;
        if (!needUpdate && networkVersion > 0)
            listener.onResponse(networkVersion);
        else
            cardRepository.networkVersion(
                    new Response.Listener<Integer>() {
                        @Override
                        public void onResponse(Integer response) {
                            networkVersion = response;
                            lastUpdated = Calendar.getInstance();
                            listener.onResponse(response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            networkVersion = -1;
                            lastUpdated = Calendar.getInstance();
                            if (errorListener != null)
                                errorListener.onErrorResponse(error);
                        }
                    });
    }

    @Override
    public void needUpdated(@NonNull final Response.Listener<Boolean> listener,
                            @Nullable final Response.ErrorListener errorListener) {
        networkVersion(new Response.Listener<Integer>() {
            @Override
            public void onResponse(Integer response) {
                listener.onResponse(response > version());
            }
        }, errorListener);
    }

    private void invalidate() {
        version = -1;
        networkVersion = -1;
        bitmapLruCache.evictAll();
        cardLruCache.evictAll();
        filterLruCache.evictAll();
        expansions = null;
        lastUpdated = null;
    }

    public static class Builder {
        @NonNull
        private final ICardRepository cardRepository;
        @NonNull
        private LruCache<String, Bitmap> bitmapLruCache;
        @NonNull
        private LruCache<Integer, Bitmap> thumbnailLruCache;
        @IntRange(from = 1)
        private int cardCacheSize;
        @IntRange(from = 1)
        private int filterCacheSize;
        @IntRange(from = 0)
        private int cacheTime;

        private Builder(@NonNull final ICardRepository cardRepository) {
            this.cardRepository = cardRepository;
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            final int cacheSize = maxMemory / 8;
            bitmapLruCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    return bitmap.getByteCount() / 1024;
                }
            };
            thumbnailLruCache = new LruCache<Integer, Bitmap>(cacheSize / 2) {
                @Override
                protected int sizeOf(Integer key, Bitmap bitmap) {
                    return bitmap.getByteCount() / 1024;
                }
            };
            cardCacheSize = 200;
            filterCacheSize = 10;
            cacheTime = 15;
        }

        public Builder setBitmapLruCache(@NonNull final LruCache<String, Bitmap> bitmapLruCache) {
            this.bitmapLruCache = bitmapLruCache;
            return this;
        }

        public void setThumbnailLruCache(@NonNull final LruCache<Integer, Bitmap> thumbnailLruCache) {
            this.thumbnailLruCache = thumbnailLruCache;
        }

        public Builder setCardCacheSize(final int cardCacheSize) {
            this.cardCacheSize = cardCacheSize;
            return this;
        }

        public Builder setFilterCacheSize(final int filterCacheSize) {
            this.filterCacheSize = filterCacheSize;
            return this;
        }

        public Builder setCacheTime(final int cacheTime) {
            this.cacheTime = cacheTime;
            return this;
        }

        public ICardRepository build() {
            return new CacheCardRepository(cardRepository, this);
        }
    }
}
