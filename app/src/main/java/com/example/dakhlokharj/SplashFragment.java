package com.example.dakhlokharj;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class SplashFragment extends Fragment {
    boolean languageSelected;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int splashDuration = 1500;
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(getString(R.string.settings_shared_preferences), Context.MODE_PRIVATE);
        boolean onboardingFinished = sharedPreferences.getBoolean(getString(R.string.onboarding_finished), false);
        languageSelected = requireActivity().getIntent().getBooleanExtra(getString(R.string.languageSelected), false);
        if (!onboardingFinished) {
            if (languageSelected) {
                splashDuration = 0;
            }
        }
        new Handler().postDelayed(() -> {
            if (onboardingFinished) {
                Intent intent = new Intent(requireContext(), MainActivity.class);
                startActivity(intent);
                requireActivity().finish();
            } else {
                if (languageSelected) {
                    Navigation.findNavController(container).navigate(R.id.action_splashFragment_to_viewPagerFragment);
                } else {
                    Navigation.findNavController(container).navigate(R.id.action_splashFragment_to_languageSelectFragment);
                }
            }
        }, splashDuration);

        return inflater.inflate(R.layout.fragment_splash, container, false);
    }
}