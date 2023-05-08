package com.example.moveit.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.example.moveit.R;
import com.example.moveit.model.theme.ThemeSharedPreferences;

import java.util.Objects;

public class SetThemeActivity extends AppCompatActivity {
    private ThemeSharedPreferences preferencesManager;
    private TextView currentThemeTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_theme);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setTitle("Change Theme");
        preferencesManager = new ThemeSharedPreferences(SetThemeActivity.this);
        currentThemeTV = findViewById(R.id.currentThemeText);

        setUpText();
        setUpButtons();
    }

    private void setUpText() {
        if (preferencesManager.getValue("currentTheme", "Dark").equals("Dark")){
            currentThemeTV.setText(R.string.currentThemeDark);
        } else if (preferencesManager.getValue("currentTheme", "Dark").equals("Light")) {
            currentThemeTV.setText(R.string.currentThemeLight);
        } else {
            currentThemeTV.setText(R.string.currentThemeSys);
        }
    }

    private void setUpButtons() {
        Button lightBtn = findViewById(R.id.lightBtn);
        Button darkBtn = findViewById(R.id.darkBtn);
        Button systemDefBtn = findViewById(R.id.systemDefaultBtn);

        lightBtn.setOnClickListener(v -> {
            preferencesManager.putValue("currentTheme", "Light");
            currentThemeTV.setText(R.string.currentThemeLight);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            recreate();
        });

        darkBtn.setOnClickListener(v -> {
            preferencesManager.putValue("currentTheme", "Dark");
            currentThemeTV.setText(R.string.currentThemeDark);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            recreate();
        });

        systemDefBtn.setOnClickListener(v -> {
            preferencesManager.putValue("currentTheme", "System");
            currentThemeTV.setText(R.string.currentThemeSys);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            recreate();
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemID = item.getItemId();

        if (itemID == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}