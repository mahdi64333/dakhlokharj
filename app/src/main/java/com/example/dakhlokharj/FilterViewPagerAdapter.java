package com.example.dakhlokharj;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class FilterViewPagerAdapter extends FragmentStateAdapter {
    public FilterViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new OrderNameFilterFragment();
            case 1:
                return new PriceFilterFragment();
            case 2:
                return new BuyerFilterFragment();
            case 3:
                return new DateFilterFragment();
            default:
                return new ConsumerFilterFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
