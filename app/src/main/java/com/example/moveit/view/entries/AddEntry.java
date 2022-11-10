package com.example.moveit.view.entries;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
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
    }

    private void setUpMoods() {
        ImageView green = findViewById(R.id.green_circle);
        ImageView yellow = findViewById(R.id.yellow_circle);
        ImageView red = findViewById(R.id.red_circle);

        green.setOnClickListener(v -> currentEntry.setMood("GOOD"));
        yellow.setOnClickListener(v -> currentEntry.setMood("OK"));
        red.setOnClickListener(v -> currentEntry.setMood("BAD"));
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