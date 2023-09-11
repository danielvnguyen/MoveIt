package com.example.moveit.view.categories;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.moveit.R;
import com.example.moveit.model.categories.Category;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;
import java.util.UUID;

public class AddCategory extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_add_category);
        setTitle(getString(R.string.add_category_title));

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        setUpButtons();
    }

    private void setUpButtons() {
        EditText categoryNameInput = findViewById(R.id.categoryNameInput);
        categoryNameInput.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.required,0);
        Button saveCategoryBtn = findViewById(R.id.saveCategoryBtn);

        saveCategoryBtn.setOnClickListener(v -> {
            saveCategoryBtn.setEnabled(false);
            String categoryText = categoryNameInput.getText().toString();
            if (categoryText.isEmpty()) {
                Toast.makeText(AddCategory.this, "Please fill out the category name", Toast.LENGTH_SHORT).show();
                saveCategoryBtn.setEnabled(true);
            } else {
                String categoryId = UUID.randomUUID().toString();
                Category newCategory = new Category(categoryText, categoryId);
                db.collection("categories").document(currentUser.getUid()).collection("categoryList")
                    .document(categoryId).set(newCategory).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(AddCategory.this, "Created category successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(AddCategory.this, "Error creating category", Toast.LENGTH_SHORT).show();
                            saveCategoryBtn.setEnabled(true);
                        }
                    });
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