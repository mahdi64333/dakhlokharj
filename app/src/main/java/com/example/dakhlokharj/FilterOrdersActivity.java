package com.example.dakhlokharj;

import android.os.Bundle;
import android.view.Menu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class FilterOrdersActivity extends AppCompatActivity {
    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager2 viewPager;
    AtomicBoolean deletedSomething;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_orders);

        tabLayout = findViewById(R.id.tabLayoutFilterActivity);
        viewPager = findViewById(R.id.viewPagerFilterActivity);
        toolbar = findViewById(R.id.toolbarFilterActivity);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        deletedSomething = new AtomicBoolean(false);

        FilterViewPagerAdapter filterViewPagerAdapter = new FilterViewPagerAdapter(FilterOrdersActivity.this, deletedSomething);
        viewPager.setAdapter(filterViewPagerAdapter);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText(getResources().getString(R.string.order_name));
                            break;
                        case 1:
                            tab.setText(getResources().getString(R.string.price));
                            break;
                        case 2:
                            tab.setText(getResources().getString(R.string.buyer_name));
                            break;
                        case 3:
                            tab.setText(getResources().getString(R.string.buy_date));
                            break;
                        case 4:
                            tab.setText(getResources().getString(R.string.consumer));
                            break;
                    }
                }).attach();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.activity_filter_toolbar_menu, menu);
        Objects.requireNonNull(toolbar.getOverflowIcon()).setTint(getResources().getColor(R.color.white));
        Objects.requireNonNull(toolbar.getNavigationIcon()).setTint(getResources().getColor(R.color.white));
        return super.onCreateOptionsMenu(menu);
    }
}