package com.example.dakhlokharj;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.settings_shared_preferences), Context.MODE_PRIVATE);
        LocaleHelper.setLocale(SplashActivity.this, sharedPreferences.getString(getString(R.string.locale), "en"));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }
}