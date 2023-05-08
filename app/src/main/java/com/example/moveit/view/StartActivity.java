package com.example.moveit.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Button;

import com.example.moveit.R;
import com.example.moveit.model.theme.ThemeSharedPreferences;
import com.example.moveit.view.account.LoginActivity;
import com.example.moveit.view.account.RegisterActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartActivity extends AppCompatActivity {
    private ThemeSharedPreferences preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        preferencesManager = new ThemeSharedPreferences(StartActivity.this);

        setUpButtons();
    }

    private void setUpTheme() {
        int currentTheme = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);

        if (preferencesManager.getValue("currentTheme", "Dark").equals("Dark")){
            switch (currentTheme) {
                case Configuration.UI_MODE_NIGHT_YES:
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    recreate();
                    break;
            }
        } else if (preferencesManager.getValue("currentTheme", "Light").equals("Light")) {
            switch (currentTheme) {
                case Configuration.UI_MODE_NIGHT_YES:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    recreate();
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                    break;
            }
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
        setUpTheme();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(StartActivity.this, HomeActivity.class));
        }
    }
}