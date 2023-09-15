package com.danielvnguyen.moveit.view.account;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Button;
import com.danielvnguyen.moveit.R;
import com.danielvnguyen.moveit.model.theme.ThemeSharedPreferences;
import com.danielvnguyen.moveit.view.homepage.HomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartActivity extends AppCompatActivity {
    private ThemeSharedPreferences preferencesManager;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        preferencesManager = new ThemeSharedPreferences(StartActivity.this);

        setUpButtons();
    }

    private void setUpTheme() {
        int currentTheme = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);

        if (currentUser != null) {
            if (preferencesManager.getValue(currentUser.getUid() + ".currentTheme", "Dark").equals("Dark")){
                switch (currentTheme) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        recreate();
                        break;
                }
            } else if (preferencesManager.getValue(currentUser.getUid() + ".currentTheme", "Light").equals("Light")) {
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

    public static Intent makeIntent(Context context) {
        return new Intent(context, StartActivity.class);
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        setUpTheme();
        if (currentUser != null) {
            startActivity(new Intent(StartActivity.this, HomeActivity.class));
        }
    }
}