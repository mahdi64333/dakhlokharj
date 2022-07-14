package com.example.dakhlokharj;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
    AppCompatSpinner spinnerApplicationLanguage;
    SharedPreferences sharedPreferences;
    int startingOrderBySelection, resultCode = 0;
    String selectedLanguage;
    ActivityResultLauncher<String> startActivityForResultImport;
    TextView buttonImport, buttonExport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        toolbar = findViewById(R.id.toolbarSettings);
        switchNightMode = findViewById(R.id.switchNightMode);
        spinnerDefaultOrderMode = findViewById(R.id.spinnerDefaultOrderBy);
        buttonImport = findViewById(R.id.buttonImport);
        buttonExport = findViewById(R.id.buttonExport);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Objects.requireNonNull(toolbar.getOverflowIcon()).setTint(getResources().getColor(R.color.white));
        Objects.requireNonNull(toolbar.getNavigationIcon()).setTint(getResources().getColor(R.color.white));

        sharedPreferences = getSharedPreferences(getString(R.string.settings_shared_preferences), Context.MODE_PRIVATE);
        switchNightMode.setChecked(sharedPreferences.getBoolean(getString(R.string.settings_night_mode), false));
        startingOrderBySelection = sharedPreferences.getInt(getString(R.string.settings_default_order), DatabaseHelper.ORDER_MODE_TIME_DESC);
        spinnerDefaultOrderMode.setSelection(startingOrderBySelection);

        spinnerApplicationLanguage = findViewById(R.id.spinnerLanguage);
        selectedLanguage = sharedPreferences.getString(getString(R.string.locale), "en");
        if (selectedLanguage.equals("en")) {
            spinnerApplicationLanguage.setSelection(0);
        } else {
            spinnerApplicationLanguage.setSelection(1);
        }

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
                editor.putInt(getString(R.string.settings_default_order), spinnerDefaultOrderMode.getSelectedItemPosition());
                if (startingOrderBySelection != spinnerDefaultOrderMode.getSelectedItemPosition()) {
                    resultCode = resultCode | 4 | 1;
                } else {
                    resultCode = resultCode & ~4 & ~1;
                }
                setResult(resultCode);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinnerApplicationLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (i == 0 && !selectedLanguage.equals("en")) {
                    LocaleHelper.setLocale(SettingsActivity.this, "en");
                    editor.putString(getString(R.string.locale), "en");
                    selectedLanguage = "en";
                    editor.apply();
                    resultCode = resultCode | 8;
                    setResult(resultCode);
                    finish();
                } else if (i == 1 && !selectedLanguage.equals("fa")) {
                    LocaleHelper.setLocale(SettingsActivity.this, "fa");
                    editor.putString(getString(R.string.locale), "fa");
                    selectedLanguage = "fa";
                    editor.apply();
                    resultCode = resultCode | 8;
                    setResult(resultCode);
                    finish();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        startActivityForResultImport = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        Log.i("path", uri.getPath());
                        resultCode = resultCode | 1;
                        setResult(resultCode);
                        if (DatabaseHelper.importDB(SettingsActivity.this, uri)) {
                            Toast.makeText(SettingsActivity.this, R.string.operation_successful, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SettingsActivity.this, R.string.operation_failed, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SettingsActivity.this, R.string.operation_failed, Toast.LENGTH_SHORT).show();
                    }
                });

        buttonImport.setOnClickListener(view -> startActivityForResultImport.launch("*/*"));

        buttonExport.setOnClickListener(view -> {
            if (DatabaseHelper.exportDB(SettingsActivity.this)) {
                Toast.makeText(SettingsActivity.this,
                        getString(R.string.file_location) +
                                Environment.getExternalStorageDirectory() +
                                "/dakhlokharj.db",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SettingsActivity.this, R.string.operation_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}