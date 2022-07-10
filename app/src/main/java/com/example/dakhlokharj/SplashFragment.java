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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(getString(R.string.settings_shared_preferences), Context.MODE_PRIVATE);
        boolean onboardingFinished = sharedPreferences.getBoolean(getString(R.string.onboarding_finished), false);
        new Handler().postDelayed(() -> {
            if (onboardingFinished) {
                Intent intent = new Intent(requireContext(), MainActivity.class);
                startActivity(intent);
                requireActivity().finish();
            } else {
                Navigation.findNavController(container).navigate(R.id.action_splashFragment_to_viewPagerFragment);
            }
        }, 1500);

        return inflater.inflate(R.layout.fragment_splash, container, false);
    }
}