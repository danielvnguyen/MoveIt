package com.example.moveit.view.entries;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.moveit.R;
import com.example.moveit.model.entries.Entry;

import java.util.Objects;

public class AddEntry extends AppCompatActivity {

    private Button saveBtn;
    private Entry currentEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_add_entry);
        setTitle(getString(R.string.add_entry_title));

        currentEntry = new Entry();

        setUpMoods();
        //setUpSaveBtn();
        setUpEntriesList();
    }

    private void setUpEntriesList() {

    }

    private void setUpMoods() {
        TextView amazingMoodBtn = findViewById(R.id.amazingMoodBtn);
        TextView greatMoodBtn = findViewById(R.id.greatMoodBtn);
        TextView goodMoodBtn = findViewById(R.id.goodMoodBtn);
        TextView mehMoodBtn = findViewById(R.id.mehMoodBtn);
        TextView badMoodBtn = findViewById(R.id.badMoodBtn);

        amazingMoodBtn.setOnClickListener(v -> currentEntry.setMood("AMAZING"));
        greatMoodBtn.setOnClickListener(v -> currentEntry.setMood("GREAT"));
        goodMoodBtn.setOnClickListener(v -> currentEntry.setMood("GOOD"));
        mehMoodBtn.setOnClickListener(v -> currentEntry.setMood("MEH"));
        badMoodBtn.setOnClickListener(v -> currentEntry.setMood("BAD"));
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