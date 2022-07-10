package com.example.dakhlokharj;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {
    Toolbar toolbar;
    SwitchCompat switchNightMode;
    AppCompatSpinner spinnerDefaultOrderMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        toolbar = findViewById(R.id.toolbarSettings);
        switchNightMode = findViewById(R.id.switchNightMode);
        spinnerDefaultOrderMode = findViewById(R.id.spinnerDefaultOrderBy);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Objects.requireNonNull(toolbar.getOverflowIcon()).setTint(getResources().getColor(R.color.white));
        Objects.requireNonNull(toolbar.getNavigationIcon()).setTint(getResources().getColor(R.color.white));

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.settings_shared_preferences), Context.MODE_PRIVATE);
        switchNightMode.setChecked(sharedPreferences.getBoolean(getString(R.string.settings_night_mode), false));
        spinnerDefaultOrderMode.setSelection(sharedPreferences.getInt(getString(R.string.settings_default_order), DatabaseHelper.ORDER_MODE_TIME_DESC));

        switchNightMode.setOnCheckedChangeListener((compoundButton, b) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(getString(R.string.settings_night_mode), b);
            editor.apply();
            if (b) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        spinnerDefaultOrderMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(getString(R.string.settings_default_order), i);
                Intent intent = new Intent();
                setResult(2, intent);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}