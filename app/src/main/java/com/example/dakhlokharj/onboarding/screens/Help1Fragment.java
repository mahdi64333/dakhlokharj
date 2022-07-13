package com.example.dakhlokharj.onboarding.screens;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.dakhlokharj.R;


public class Help1Fragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help1, container, false);

        TextView tvRenameHelp = view.findViewById(R.id.textViewRenameHelp);
        TextView tvDeleteHelp = view.findViewById(R.id.textViewDeleteHelp);

        ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    tvDeleteHelp.setWidth(tvRenameHelp.getWidth());
                }
            });
        }

        Button next = view.findViewById(R.id.buttonHelp1Next);
        ViewPager2 viewPager = requireActivity().findViewById(R.id.viewPagerHelp);
        next.setOnClickListener(v -> viewPager.setCurrentItem(1));
        return view;
    }
}