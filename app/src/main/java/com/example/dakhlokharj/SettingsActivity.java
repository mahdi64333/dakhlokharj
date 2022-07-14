package com.example.dakhlokharj;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

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
    ActivityResultLauncher<Intent> getFullAccessPermission;
    TextView buttonImport, buttonExport;
    final int PERMISSION_REQUEST_CODE = 1;
    final String IMPORT_REQUEST = "import";
    final String EXPORT_REQUEST = "export";
    String userRequestImportExport;


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
                        if (DatabaseHelper.importDB(SettingsActivity.this, uri)) {
                            Toast.makeText(SettingsActivity.this, R.string.operation_successful, Toast.LENGTH_SHORT).show();
                            resultCode = resultCode | 1;
                            setResult(resultCode);
                        } else {
                            Toast.makeText(SettingsActivity.this, R.string.operation_failed, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SettingsActivity.this, R.string.operation_failed, Toast.LENGTH_SHORT).show();
                    }
                });

        getFullAccessPermission = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
                        if (askForStoragePermission()) {
                            if (userRequestImportExport.equals(IMPORT_REQUEST)) {
                                importDbCallback();
                            } else if (userRequestImportExport.equals(EXPORT_REQUEST)) {
                                exportDbCallback();
                            }
                        }
                    }
                });

        buttonImport.setOnClickListener(view -> {
            if (!checkStoragePermission(IMPORT_REQUEST)) {
                return;
            }
            importDbCallback();
        });

        buttonExport.setOnClickListener(view -> {
            if (!checkStoragePermission(EXPORT_REQUEST)) {
                return;
            }
            exportDbCallback();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    public void importDbCallback() {
        startActivityForResultImport.launch("*/*");
    }

    public void exportDbCallback() {
        if (DatabaseHelper.exportDB(SettingsActivity.this)) {
            Toast.makeText(SettingsActivity.this,
                    getString(R.string.file_location) +
                            Environment.getExternalStorageDirectory() +
                            "/dakhlokharj.db",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(SettingsActivity.this, R.string.operation_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkStoragePermission(String requestMode) {
        userRequestImportExport = requestMode;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R &&
                    !Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                getFullAccessPermission.launch(intent);
                return false;
            }
        }
        return askForStoragePermission();
    }

    private boolean askForStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                (getApplicationContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        getApplicationContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(SettingsActivity.this, permissions, PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length == 2 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.R ||
                    Environment.isExternalStorageManager())) {
                if (userRequestImportExport.equals(IMPORT_REQUEST)) {
                    importDbCallback();
                } else if (userRequestImportExport.equals(EXPORT_REQUEST)) {
                    exportDbCallback();
                }
            } else {
                Toast.makeText(SettingsActivity.this, R.string.no_storage_access, Toast.LENGTH_SHORT).show();
            }
        }
    }
}