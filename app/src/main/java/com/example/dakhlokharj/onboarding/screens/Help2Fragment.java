package com.example.dakhlokharj.onboarding.screens;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.dakhlokharj.MainActivity;
import com.example.dakhlokharj.R;

public class Help2Fragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help2, container, false);
        Button next = view.findViewById(R.id.buttonHelp2Next);
        next.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences(getString(R.string.settings_shared_preferences), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(getString(R.string.onboarding_finished), true);
            editor.apply();
            Intent intent = new Intent(requireContext(), MainActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });
        return view;
    }
}