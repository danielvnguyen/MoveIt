package com.example.moveit.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;

import com.example.moveit.R;
import com.example.moveit.model.Entry;

import java.util.Objects;

public class AddEntry extends AppCompatActivity {

    private Button saveBtn;
    private ImageView green;
    private ImageView yellow;
    private ImageView red;
    private Entry currentEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_add_entry);

        //setUpMoods();
        //setUpSaveBtn();
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