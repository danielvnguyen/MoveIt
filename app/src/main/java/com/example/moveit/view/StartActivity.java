package com.example.moveit.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.moveit.R;
import com.example.moveit.model.ThemeSharedPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        setUpTheme();
        setUpButtons();
    }

    private void setUpTheme() {
        ThemeSharedPreferences preferencesManager = new ThemeSharedPreferences(StartActivity.this);
        if (preferencesManager.getValue("currentTheme", "Dark").equals("Dark")){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if (preferencesManager.getValue("currentTheme", "Dark").equals("Light")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    private void setUpButtons() {
        Button registerBtn = findViewById(R.id.register);
        Button loginBtn = findViewById(R.id.login);

        registerBtn.setOnClickListener((v) -> {
            Intent intent = RegisterActivity.makeIntent(this);
            startActivity(intent);
        });

        loginBtn.setOnClickListener((v) -> {
            Intent intent = LoginActivity.makeIntent(this);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(StartActivity.this, HomeActivity.class));
        }
    }
}