package com.example.moveit.view.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.moveit.R;

import java.util.Objects;

public class AddActivity extends AppCompatActivity {
    private String categoryName;
    private String categoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_activity);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        setUpInterface();
    }

    private void setUpInterface() {
        Bundle extras = getIntent().getExtras();
        categoryName = extras.get("categoryName").toString();
        categoryId = extras.get("categoryId").toString();
        setTitle("New " + categoryName + " Activity");
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

    public static Intent makeIntent(Context context) {
        return new Intent(context, AddActivity.class);
    }
}