package com.example.dakhlokharj;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.concurrent.atomic.AtomicBoolean;

public class FilterViewPagerAdapter extends FragmentStateAdapter {
    AtomicBoolean deletedSomething;

    public FilterViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, AtomicBoolean deletedSomething) {
        super(fragmentActivity);
        this.deletedSomething = deletedSomething;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new OrderNameFilterFragment(deletedSomething);
            case 1:
                return new PriceFilterFragment(deletedSomething);
            case 2:
                return new BuyerFilterFragment(deletedSomething);
            case 3:
                return new DateFilterFragment(deletedSomething);
            default:
                return new ConsumerFilterFragment(deletedSomething);
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
