package com.joshuaavalon.wsdeckeditor;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.joshuaavalon.wsdeckeditor.sdk.Card;

import java.util.ArrayList;
import java.util.List;

public class CardActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {
    private static final String EXTRA_TITLE = "CardActivity.extra.Title";
    private static final String EXTRA_CARDS = "CardActivity.extra.Cards";
    private static final String EXTRA_POSITION = "CardActivity.extra.Position";
    public static final String RESULT_POSITION = "CardActivity.extra.Position";
    private List<Card> cards;
    private int position;

    public static void start(@NonNull final Fragment fragment, final int requestCode,
                             @Nullable final String title, @NonNull final ArrayList<Card> cards,
                             @IntRange(from = 0) final int position) {
        final Intent intent = new Intent(fragment.getContext(), CardActivity.class);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_CARDS, cards);
        intent.putExtra(EXTRA_POSITION, position);
        fragment.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);
        final Intent intent = getIntent();
        initToolbar(intent.getStringExtra(EXTRA_TITLE));
        int position = 0;
        cards = intent.getParcelableArrayListExtra(EXTRA_CARDS);
        final int intentPosition = intent.getIntExtra(EXTRA_POSITION, 0);
        if (intentPosition >= 0 && cards != null && intentPosition < cards.size())
            position = intentPosition;
        if (cards == null)
            cards = new ArrayList<>();
        initViewPager(position);
    }

    private void initToolbar(@Nullable final String title) {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;
        actionBar.setDisplayHomeAsUpEnabled(true);
        if (title == null)
            actionBar.setDisplayShowTitleEnabled(false);
        else {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(title);
        }
    }

    private void initViewPager(final int startPosition) {
        final CardPagerAdapter adapter = new CardPagerAdapter(getSupportFragmentManager());
        final ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(this);
        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(startPosition);

        //TODO
/*        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeckSelectDialog(serials.get(viewPager.getCurrentItem()));
            }
        });*/
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        this.position = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private class CardPagerAdapter extends FragmentPagerAdapter {

        public CardPagerAdapter(@NonNull final FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            return CardDetailFragment.newInstance(cards.get(position));
        }

        @Override
        public int getCount() {
            return cards.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return cards.get(position).getSerial();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                setResultCancel();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setResultCancel() {
        final Intent resultIntent = new Intent();
        resultIntent.putExtra(RESULT_POSITION, position);
        setResult(RESULT_CANCELED, resultIntent);
    }

    @Override
    public void onBackPressed() {
        setResultCancel();
        super.onBackPressed();
    }
}
