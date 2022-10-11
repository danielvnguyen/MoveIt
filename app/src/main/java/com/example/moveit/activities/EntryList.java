package com.example.moveit.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.moveit.R;

public class EntryList extends AppCompatActivity {
    private Button registerBtn;
    private Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_list);

        registerBtn = findViewById(R.id.register);
        loginBtn = findViewById(R.id.login);

        registerBtn.setOnClickListener((v) -> {
            Intent intent = RegisterActivity.makeIntent(this);
            startActivity(intent);
        });

        loginBtn.setOnClickListener((v) -> {
            Intent intent = LoginActivity.makeIntent(this);
            startActivity(intent);
        });
    }
}