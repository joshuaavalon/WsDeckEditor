package com.joshuaavalon.wsdeckeditor.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;

import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.view.CardPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class CardViewActivity extends BaseActivity {
    private static final String ARG_SERIALS = "serials";
    private static final String ARG_POSITION = "position";
    private List<String> serials;

    public static void start(@NonNull final Context context,
                             @NonNull final List<String> serials,
                             @IntRange(from = 0) final int position) {
        final Intent intent = new Intent(context, CardViewActivity.class);
        intent.putExtra(ARG_SERIALS, new ArrayList<>(serials));
        intent.putExtra(ARG_POSITION, position);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_view);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        final Intent intent = getIntent();
        int position = 0;
        if (intent != null) {
            serials = intent.getStringArrayListExtra(ARG_SERIALS);
            final int intentPosition = intent.getIntExtra(ARG_POSITION, 0);
            if (intentPosition >= 0 && intentPosition < serials.size())
                position = intentPosition;
        }
        if (serials == null)
            serials = new ArrayList<>();
        final CardPagerAdapter adapter = new CardPagerAdapter(getSupportFragmentManager(), serials);
        viewPager.setAdapter(adapter);
        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(position);
    }
}
