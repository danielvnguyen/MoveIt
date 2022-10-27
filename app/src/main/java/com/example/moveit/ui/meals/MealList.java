package com.example.moveit.ui.meals;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.moveit.R;
import com.example.moveit.model.Meal;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Objects;

public class MealList extends AppCompatActivity {

    private FloatingActionButton addMealBtn;
    private ArrayAdapter<Meal> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_meal_list);

        addMealBtn = findViewById(R.id.addMeal);

        setUpMealList();
        setUpAddMealBtn();
    }

    protected void onStart() {
        super.onStart();
        listAdapter.notifyDataSetChanged();
    }

    private void setUpMealList() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        ListView mealListView = findViewById(R.id.mealList);
        ArrayList<Meal> mealList = new ArrayList<>();
        listAdapter = new ArrayAdapter<>(this, R.layout.meal_list_item, mealList);
        mealListView.setAdapter(listAdapter);
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