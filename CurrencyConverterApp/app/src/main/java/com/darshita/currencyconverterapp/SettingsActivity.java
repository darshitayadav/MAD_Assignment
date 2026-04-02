package com.darshita.currencyconverterapp; // <-- Ensure this is here!

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat switchTheme;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }

        switchTheme = findViewById(R.id.switch_settings);
        sharedPreferences = getSharedPreferences("ThemePrefs", MODE_PRIVATE);

        boolean isDarkTheme = sharedPreferences.getBoolean("isDarkTheme", false);
        switchTheme.setChecked(isDarkTheme);

        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isDarkTheme", isChecked);
            editor.apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        Button buttonback;
        buttonback = findViewById(R.id.button2);

        buttonback.setOnClickListener( v -> {
            Intent mainpage  = new Intent(SettingsActivity.this, MainActivity.class);
            startActivity(mainpage);
        });
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}