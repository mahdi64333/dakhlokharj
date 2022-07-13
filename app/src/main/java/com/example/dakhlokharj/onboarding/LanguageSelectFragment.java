package com.example.dakhlokharj.onboarding;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.dakhlokharj.LocaleHelper;
import com.example.dakhlokharj.R;
import com.example.dakhlokharj.SplashActivity;

public class LanguageSelectFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_language_select, container, false);
        Button btnEnglish = view.findViewById(R.id.buttonChoseEnglish);
        Button btnFarsi = view.findViewById(R.id.buttonChoseFarsi);

        btnEnglish.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences(
                    getString(R.string.settings_shared_preferences),
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(getString(R.string.locale), "en");
            editor.apply();
            LocaleHelper.setLocale(requireActivity(), "en");
            Intent intent = new Intent(requireContext(), SplashActivity.class);
            intent.putExtra(getString(R.string.languageSelected), true);
            startActivity(intent);
            requireActivity().finish();
        });
        btnFarsi.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences(
                    getString(R.string.settings_shared_preferences),
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(getString(R.string.locale), "fa");
            editor.apply();
            LocaleHelper.setLocale(requireActivity(), "fa");
            Intent intent = new Intent(requireContext(), SplashActivity.class);
            intent.putExtra(getString(R.string.languageSelected), true);
            startActivity(intent);
            requireActivity().finish();
        });
        return view;
    }
}