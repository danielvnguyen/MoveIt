package com.example.moveit.view.entries;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.moveit.R;
import com.example.moveit.model.entries.Entry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class AddEntry extends AppCompatActivity {

    private Button saveBtn;
    private Entry currentEntry;

    private EditText dateInput;
    private EditText timeInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_add_entry);
        setTitle(getString(R.string.add_entry_title));

        currentEntry = new Entry();

        setUpMoods();
        setUpInterface();
        //setUpSaveBtn();
    }

    private void setUpInterface() {
        dateInput = findViewById(R.id.entryDateInput);
        timeInput = findViewById(R.id.entryTimeInput);

        long currentTime = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy h:mm a");
        Date resultDate = new Date(currentTime);
        String dateTimeText = sdf.format(resultDate);

        String dateText = dateTimeText.substring(0, dateTimeText.length() - 9);
        String timeText = dateTimeText.substring(dateTimeText.length() - 9);
        dateInput.setText(dateText);
        timeInput.setText(timeText);
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