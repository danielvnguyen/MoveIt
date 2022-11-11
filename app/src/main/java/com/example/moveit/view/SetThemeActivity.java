package com.example.moveit.view;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.moveit.R;

public class SetThemeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_theme);
        setTitle("Change Theme");
    }
}