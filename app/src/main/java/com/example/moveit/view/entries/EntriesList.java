package com.example.moveit.view.entries;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.example.moveit.R;
import com.example.moveit.view.StartActivity;
import com.example.moveit.view.activities.ActivitiesList;
import com.example.moveit.view.meals.MealList;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

public class EntriesList extends AppCompatActivity {

    private Button logoutBtn;
    private FloatingActionButton addEntryBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entries_list);

        logoutBtn = findViewById(R.id.logout);
        addEntryBtn = findViewById(R.id.addEntry);

        setUpLogOutBtn();
        setUpAddEntryBtn();
        setUpMealsBtn();
        setUpActivitiesBtn();
    }

    private void setUpMealsBtn() {
        Button editMealsBtn = findViewById(R.id.editMeals);
        editMealsBtn.setOnClickListener(v -> startActivity(new Intent(EntriesList.this, MealList.class)));
    }

    private void setUpActivitiesBtn() {
        Button editActivitiesBtn = findViewById(R.id.editActivities);
        editActivitiesBtn.setOnClickListener(v -> startActivity(new Intent(EntriesList.this, ActivitiesList.class)));
    }

    private void setUpLogOutBtn() {
        logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(EntriesList.this, "Log Out Successful", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void setUpAddEntryBtn() {
        addEntryBtn.setOnClickListener(v -> startActivity(new Intent(EntriesList.this, AddEntry.class)));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}