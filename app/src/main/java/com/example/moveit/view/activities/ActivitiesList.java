package com.example.moveit.view.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.moveit.R;
import com.example.moveit.view.meals.AddMeal;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class ActivitiesList extends AppCompatActivity {

    private String categoryId;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private String originalCategoryName;
    private EditText categoryNameInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities_list);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        setUpInterface();
        setUpButtons();
    }

    private void setUpButtons() {
        //Title will be 'new [this category] activity
        FloatingActionButton addNewActivityButton = findViewById(R.id.addNewActivityBtn);
        Button saveCategoryBtn = findViewById(R.id.saveCategoryBtn);
        Button deleteCategoryBtn = findViewById(R.id.deleteCategoryBtn);

        saveCategoryBtn.setOnClickListener(v -> {
            String newCategoryName = categoryNameInput.getText().toString();
            if (newCategoryName.equals(originalCategoryName)) {
                Toast.makeText(ActivitiesList.this, "Category name is the same", Toast.LENGTH_SHORT).show();
            } else if (newCategoryName.isEmpty()) {
                Toast.makeText(ActivitiesList.this, "Please fill in category name", Toast.LENGTH_SHORT).show();
            } else {
                db.collection("categories").document(currentUser.getUid())
                        .collection("categoryList").document(categoryId)
                        .update("name", newCategoryName).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(ActivitiesList.this, "Updated category name successfully!", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(ActivitiesList.this, "Error updating category name", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        deleteCategoryBtn.setOnClickListener(v -> {
            DocumentReference selectedCategory = db.collection("categories")
                    .document(currentUser.getUid()).collection("categoryList").document(categoryId);

            selectedCategory.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ActivitiesList.this, "Successfully deleted category & activities", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ActivitiesList.this, "Error deleting category & activities", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void setUpInterface() {
        categoryNameInput = findViewById(R.id.categoryNameInput);

        Bundle extras = getIntent().getExtras();
        originalCategoryName = extras.get("categoryName").toString();
        categoryId = extras.get("categoryId").toString();
        setTitle(originalCategoryName);
        categoryNameInput.setText(originalCategoryName);
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
        return new Intent(context, ActivitiesList.class);
    }
}