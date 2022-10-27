package com.example.moveit.ui.meals;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.example.moveit.R;
import com.example.moveit.ui.entries.AddEntry;
import com.example.moveit.ui.entries.EntriesList;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

public class MealList extends AppCompatActivity {

    private FloatingActionButton addMealBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_meal_list);

        addMealBtn = findViewById(R.id.addMeal);

        setUpAddMealBtn();
    }

    private void setUpAddMealBtn() {
        addMealBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MealList.this, AddMeal.class));
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