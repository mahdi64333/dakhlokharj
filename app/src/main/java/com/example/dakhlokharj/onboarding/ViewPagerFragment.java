package com.example.dakhlokharj.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.dakhlokharj.R;
import com.example.dakhlokharj.onboarding.screens.Help1Fragment;
import com.example.dakhlokharj.onboarding.screens.Help2Fragment;

import java.util.ArrayList;

public class ViewPagerFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_page, container, false);
        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new Help1Fragment());
        fragments.add(new Help2Fragment());
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(
                fragments,
                requireActivity().getSupportFragmentManager(),
                getLifecycle()
        );
        ViewPager2 viewPager = view.findViewById(R.id.viewPagerHelp);
        viewPager.setAdapter(viewPagerAdapter);
        return view;
    }
}