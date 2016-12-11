package com.joshuaavalon.wsdeckeditor.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import com.google.common.collect.Multiset;
import com.joshuaavalon.android.view.ContentView;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.config.CardOrder;
import com.joshuaavalon.wsdeckeditor.config.Constant;
import com.joshuaavalon.wsdeckeditor.sdk.card.Card;
import com.joshuaavalon.wsdeckeditor.sdk.deck.Deck;
import com.joshuaavalon.wsdeckeditor.util.AnimeUtils;
import com.joshuaavalon.wsdeckeditor.util.BitmapUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import timber.log.Timber;

@ContentView(R.layout.activity_deck_image)
public class DeckImageActivity extends BaseActivity {
    private static final String ARG_ID = "DeckImageActivity.Id";
    @BindView(R.id.frame_layout)
    FrameLayout frameLayout;
    private SurfaceView surfaceView;
    private Deck deck;

    public static void start(@NonNull final Context context, final long id,
                             @NonNull final View view) {
        final Intent intent = new Intent(context, DeckImageActivity.class);
        final Bundle args = new Bundle();
        args.putLong(ARG_ID, id);
        intent.putExtras(args);
        context.startActivity(intent, AnimeUtils.createRevealOption(view));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final long id = getIntent().getLongExtra(ARG_ID, 0);
        if (id <= 0) {
            Timber.w("DeckImageActivity: Empty argument");
            return;
        }
        deck = getDeckRepository().deck(id);
        if (deck == null || deck.getCardList().size() > Constant.DeckSize)
            return;
        setTitle(deck.getName());
        surfaceView = new DeckSurfaceView(this);
        frameLayout.addView(surfaceView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.deck_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.action_share:
                final Bitmap bitmap = Bitmap.createBitmap(surfaceView.getWidth(), surfaceView.getHeight(), Bitmap.Config.ARGB_8888);
                final Canvas canvas = new Canvas(bitmap);
                surfaceView.draw(canvas);
                BitmapUtils.share(this, bitmap);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void initializeActionBar(@NonNull ActionBar actionBar) {
        super.initializeActionBar(actionBar);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private class DeckSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder holder;

        public DeckSurfaceView(Context context) {
            super(context);
            holder = getHolder();
            holder.addCallback(this);
        }

        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            setWillNotDraw(false);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            clearCanvas(canvas);
            drawDeck(canvas);
        }

        private void clearCanvas(@NonNull final Canvas canvas) {
            final TypedValue value = new TypedValue();
            // Set to theme background
            getTheme().resolveAttribute(android.R.attr.colorBackground, value, true);
            int color;
            if (value.type >= TypedValue.TYPE_FIRST_COLOR_INT && value.type <= TypedValue.TYPE_LAST_COLOR_INT)
                color = value.data;
            else
                color = Color.WHITE; // If the background is not a color, use white instead
            canvas.drawColor(color);
        }

        private void drawDeck(@NonNull final Canvas canvas) {
            final List<Multiset.Entry<Card>> entryList = new ArrayList<>();
            for (Multiset.Entry<Card> cardEntry : deck.getCardList().entrySet()) {
                entryList.add(cardEntry);
            }
            if (entryList.size() == 0) return;
            Collections.sort(entryList, CardOrder.Detail.getComparator());
            Bitmap bitmap = getBitmap(entryList.get(0).getElement());
            final int row = 7;
            final int column = 8;
            final Pair<Integer, Integer> scaledSize = calculateScaledSize(bitmap.getWidth(),
                    bitmap.getHeight(), canvas.getWidth(), canvas.getHeight(), row, column);
            final int rightMargin = (canvas.getWidth() - scaledSize.first * column) / 2;
            final int topMargin = (canvas.getHeight() - scaledSize.second * row) / 2;
            int counter = 0;
            for (Multiset.Entry<Card> cardEntry : entryList) {
                bitmap = Bitmap.createScaledBitmap(getBitmap(cardEntry.getElement()),
                        scaledSize.first, scaledSize.second, false);
                for (int i = 0; i < cardEntry.getCount(); i++) {
                    canvas.drawBitmap(bitmap, rightMargin + counter % column * scaledSize.first,
                            topMargin + counter / column * scaledSize.second, null);
                    counter++;
                }
            }
        }

        // Width, Height
        private Pair<Integer, Integer> calculateScaledSize(final int bitmapWidth, final int bitmapHeight,
                                                           final int surfaceWidth, final int surfaceHeight,
                                                           final int row, final int column) {
            int cardHeight = surfaceHeight / row;
            int cardWidth = bitmapWidth * cardHeight / bitmapHeight;
            if (cardWidth * column <= surfaceWidth)
                return new Pair<>(cardWidth, cardHeight);
            cardWidth = surfaceWidth / column;
            cardHeight = bitmapHeight * cardWidth / bitmapWidth;
            return new Pair<>(cardWidth, cardHeight);
        }

        private Bitmap getBitmap(@NonNull final Card card) {
            Bitmap bitmap = getCardRepository().imageOf(card);
            if (card.getType() == Card.Type.Climax)
                bitmap = BitmapUtils.rotate(bitmap, 90);
            return bitmap;
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        }
    }
}
