package com.example.moveit.view.account;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.moveit.R;
import com.example.moveit.view.StartActivity;

import java.util.Objects;

public class AccountSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.account_settings_title));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_account_settings);

        setUpButtons();
    }

    private void setUpButtons() {
        Button changePasswordBtn = findViewById(R.id.changePasswordBtn);
        Button updateEmailBtn = findViewById(R.id.updateEmailBtn);
        Button deleteAccountBtn = findViewById(R.id.deleteAccountBtn);

        changePasswordBtn.setOnClickListener(view -> {
            Intent intent = ChangePasswordActivity.makeIntent(this);
            startActivity(intent);
        });
        updateEmailBtn.setOnClickListener(view -> {
            Intent intent = UpdateEmailActivity.makeIntent(this);
            startActivity(intent);
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