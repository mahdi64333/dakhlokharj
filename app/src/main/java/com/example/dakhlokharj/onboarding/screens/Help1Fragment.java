package com.example.dakhlokharj.onboarding.screens;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.dakhlokharj.R;


public class Help1Fragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help1, container, false);
        Button next = view.findViewById(R.id.buttonHelp1Next);
        ViewPager2 viewPager = requireActivity().findViewById(R.id.viewPagerHelp);
        next.setOnClickListener(v -> {
            viewPager.setCurrentItem(1) ;
        });
        return view;
    }
}