package com.example.moveit.view.activities;

import androidx.appcompat.app.AlertDialog;
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
import com.example.moveit.model.activities.Activity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;
import java.util.UUID;

public class AddActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private String categoryId;
    private String originalActivityName;
    private String activityId;
    private EditText activityNameInput;

    private Button saveBtn;
    private Button deleteBtn;

    private boolean isNew = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_activity);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        setUpInterface();
        setUpButtons();
    }

    private void setUpButtons() {
        saveBtn.setOnClickListener(v -> {
            if (isNew) {
                handleSave();
            } else {
                handleUpdate();
            }
        });

        deleteBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle(R.string.confirm_delete_activity);
            builder.setMessage(R.string.no_takesies_backsies);
            builder.setPositiveButton(R.string.yes, (dialog, which) -> handleDelete());
            builder.setNegativeButton(R.string.no, (dialog, which) -> {});
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    private void handleDelete() {
        db.collection("categories").document(currentUser.getUid())
                .collection("categoryList").document(categoryId)
                .collection("activityList").document(activityId).delete().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AddActivity.this, "Deleted activity successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AddActivity.this, "Error deleting activity", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleUpdate() {
        String activityName = activityNameInput.getText().toString();
        if (activityName.isEmpty()) {
            Toast.makeText(AddActivity.this, "Please fill out the activity name", Toast.LENGTH_SHORT).show();
        } else if (activityName.equals(originalActivityName)) {
            Toast.makeText(AddActivity.this, "You have made no changes!", Toast.LENGTH_SHORT).show();
        } else {
            db.collection("categories").document(currentUser.getUid())
                    .collection("categoryList").document(categoryId)
                    .collection("activityList").document(activityId).update("name", activityName)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(AddActivity.this, "Updated activity successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(AddActivity.this, "Error updating activity", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void handleSave() {
        String activityName = activityNameInput.getText().toString();
        if (!activityName.isEmpty()) {
            String activityId = UUID.randomUUID().toString();
            Activity newActivity = new Activity(activityName, categoryId, activityId);
            db.collection("categories").document(currentUser.getUid())
                    .collection("categoryList").document(categoryId)
                    .collection("activityList").document(activityId).set(newActivity)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(AddActivity.this, "Saved activity successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(AddActivity.this, "Error saving activity", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(AddActivity.this, "Please fill out the activity name", Toast.LENGTH_SHORT).show();
        }
    }

    private void setUpInterface() {
        activityNameInput = findViewById(R.id.activityNameInput);
        activityNameInput.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.required,0);
        saveBtn = findViewById(R.id.saveActivityBtn);
        deleteBtn = findViewById(R.id.deleteActivityBtn);

        if (getIntent().getExtras().size() == 2) {
            isNew = true;

            Bundle extras = getIntent().getExtras();
            String categoryName = extras.get("categoryName").toString();
            setTitle(getString(R.string.new_activity_title) + categoryName);
            categoryId = extras.get("categoryId").toString();
        } else if (getIntent().getExtras().size() == 3) {
            Bundle extras = getIntent().getExtras();
            originalActivityName = extras.get("activityName").toString();
            setTitle(getString(R.string.edit_activity_title) + originalActivityName);
            activityId = extras.get("activityId").toString();
            categoryId = extras.get("categoryId").toString();

            deleteBtn.setVisibility(View.VISIBLE);
            activityNameInput.setText(originalActivityName);
        }
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
        return new Intent(context, AddActivity.class);
    }
}