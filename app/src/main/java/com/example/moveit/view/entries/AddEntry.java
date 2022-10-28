package com.example.moveit.view.entries;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import com.example.moveit.R;
import com.example.moveit.model.Entry;

import java.util.Objects;

public class AddEntry extends AppCompatActivity {

    private Button saveBtn;
    private Entry currentEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_add_entry);

        currentEntry = new Entry();

        setUpMoods();
        //setUpSaveBtn();
    }

    private void setUpMoods() {
        ImageView green = findViewById(R.id.green_circle);
        ImageView yellow = findViewById(R.id.yellow_circle);
        ImageView red = findViewById(R.id.red_circle);

        green.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentEntry.setMood("GOOD");
            }
        });
        yellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentEntry.setMood("OK");
            }
        });
        red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentEntry.setMood("BAD");
            }
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