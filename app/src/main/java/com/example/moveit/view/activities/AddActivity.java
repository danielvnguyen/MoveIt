package com.example.moveit.view.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.moveit.R;
import com.example.moveit.model.activities.Activity;
import com.example.moveit.model.entries.Entry;
import com.example.moveit.view.HomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
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
        String newActivityName = activityNameInput.getText().toString();
        if (newActivityName.isEmpty()) {
            Toast.makeText(AddActivity.this, "Please fill out the activity name", Toast.LENGTH_SHORT).show();
        } else if (newActivityName.equals(originalActivityName)) {
            Toast.makeText(AddActivity.this, "You have made no changes!", Toast.LENGTH_SHORT).show();
        } else {
            CollectionReference activitiesRef = db.collection("categories").document(currentUser.getUid()).collection("categoryList")
                    .document(categoryId).collection("activityList");
            Query queryActivitiesByName = activitiesRef.whereEqualTo("name", newActivityName);
            queryActivitiesByName.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (!Objects.requireNonNull(task.getResult()).isEmpty()) {
                        Toast.makeText(AddActivity.this, "An activity with this name already exists!", Toast.LENGTH_SHORT).show();
                    } else {
                        CollectionReference entryListRef = db.collection("entries").document(currentUser.getUid()).collection("entryList");
                        //Handle propagation (to entry list)
                        entryListRef.get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                                        //For each entry, check if current activity is inside that entry's activities
                                        Entry currentEntry = queryDocumentSnapshots.getDocuments().get(i).toObject(Entry.class);
                                        assert currentEntry != null;
                                        HashMap<String, ArrayList<String>> entryActivities = currentEntry.getActivities();

                                        if (entryActivities.containsKey(originalActivityName) &&
                                                Objects.requireNonNull(
                                                        entryActivities.get(originalActivityName)).contains(categoryId)) {
                                            ArrayList<String> currentCategoryIds = entryActivities.get(originalActivityName);
                                            ArrayList<String> newCategoryIds = new ArrayList<>();
                                            assert currentCategoryIds != null;

                                            currentCategoryIds.remove(categoryId);
                                            newCategoryIds.add(categoryId);
                                            //Account for duplicate activity names
                                            if (currentCategoryIds.isEmpty()) {
                                                entryActivities.remove(originalActivityName);
                                            }
                                            if (entryActivities.containsKey(newActivityName)) {
                                                newCategoryIds.addAll(Objects.requireNonNull(entryActivities.get(newActivityName)));
                                            }
                                            entryActivities.put(newActivityName, newCategoryIds);

                                            String documentId = queryDocumentSnapshots.getDocuments().get(i).getId();
                                            entryListRef.document(documentId).update("activities", entryActivities).addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful()) {
                                                    //Now update the activity name in the activity list
                                                    db.collection("categories").document(currentUser.getUid())
                                                            .collection("categoryList").document(categoryId)
                                                            .collection("activityList").document(activityId).update("name", newActivityName)
                                                            .addOnCompleteListener(task2 -> {
                                                                if (task2.isSuccessful()) {
                                                                    Toast.makeText(AddActivity.this, "Updated activity successfully!", Toast.LENGTH_SHORT).show();
                                                                    Intent intent = new Intent(this, HomeActivity.class);
                                                                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                                                    intent.putExtra("isChangedEntries", true);
                                                                    startActivity(intent);
                                                                } else {
                                                                    Toast.makeText(AddActivity.this, "Error updating activity", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                } else {
                                                    Log.d("AddActivity", "Error updating entry activities: ", task.getException());
                                                }
                                            });
                                        }
                                    }
                                });
                    }
                }
            });
        }
    }

    private void handleSave() {
        String activityName = activityNameInput.getText().toString();
        if (!activityName.isEmpty()) {
            CollectionReference activitiesRef = db.collection("categories").document(currentUser.getUid()).collection("categoryList")
                    .document(categoryId).collection("activityList");
            Query queryActivitiesByName = activitiesRef.whereEqualTo("name", activityName);
            queryActivitiesByName.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (!Objects.requireNonNull(task.getResult()).isEmpty()) {
                        Toast.makeText(AddActivity.this, "An activity with this name already exists!", Toast.LENGTH_SHORT).show();
                    } else {
                        String activityId = UUID.randomUUID().toString();
                        Activity newActivity = new Activity(activityName, categoryId, activityId);
                        db.collection("categories").document(currentUser.getUid())
                                .collection("categoryList").document(categoryId)
                                .collection("activityList").document(activityId).set(newActivity)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(AddActivity.this, "Saved activity successfully!", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(AddActivity.this, "Error saving activity", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
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
            activityNameInput.setHint(categoryName + " " + getString(R.string.activity_name));
            setTitle(getString(R.string.new_activity_title));
            categoryId = extras.get("categoryId").toString();
        } else if (getIntent().getExtras().size() == 3) {
            Bundle extras = getIntent().getExtras();
            originalActivityName = extras.get("activityName").toString();
            setTitle(getString(R.string.edit_activity_title));
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