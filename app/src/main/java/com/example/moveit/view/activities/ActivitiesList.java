package com.example.moveit.view.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.example.moveit.R;
import com.example.moveit.model.activities.Activity;
import com.example.moveit.model.activities.ActivityListAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Objects;

public class ActivitiesList extends AppCompatActivity {

    private String categoryId;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private String originalCategoryName;
    private EditText categoryNameInput;
    private ArrayAdapter<Activity> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities_list);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        setUpInterface();
        setUpButtons();
        setUpActivitiesList();
    }

    private void setUpActivitiesList() {
        ListView activityListView = findViewById(R.id.activityListView);
        activityListView.setEmptyView(findViewById(R.id.emptyTV));

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        adapter = new ActivityListAdapter(ActivitiesList.this, R.layout.item_activity);
        activityListView.setAdapter(adapter);

        db.collection("categories").document(currentUser.getUid()).collection("categoryList")
                .document(categoryId).collection("activityList").get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Activity> activityList = new ArrayList<>();
                        for (QueryDocumentSnapshot activityDoc : Objects.requireNonNull(task.getResult())) {
                            Activity currentActivity = activityDoc.toObject(Activity.class);
                            activityList.add(currentActivity);
                        }

                        adapter.clear();
                        adapter.addAll(activityList);
                        progressDialog.dismiss();
                    } else {
                        Log.d("ActivitiesList", "Error retrieving documents: ", task.getException());
                    }
                });
    }

    private void setUpButtons() {
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
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle(R.string.confirm_delete_category);
            builder.setMessage(R.string.no_takesies_backsies);
            builder.setPositiveButton(R.string.yes, (dialog, which) -> handleDelete());
            builder.setNegativeButton(R.string.no, (dialog, which) -> {});
            AlertDialog dialog = builder.create();
            dialog.show();
        });
        addNewActivityButton.setOnClickListener(v -> {
            Intent intent = AddActivity.makeIntent(this);
            intent.putExtra("categoryName", originalCategoryName);
            intent.putExtra("categoryId", categoryId);
            startActivity(intent);
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

    private void handleDelete() {
        DocumentReference selectedCategory = db.collection("categories")
                .document(currentUser.getUid()).collection("categoryList").document(categoryId);
        selectedCategory.collection("activityList").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot activityDoc : Objects.requireNonNull(task.getResult())) {
                    Activity currentActivity = activityDoc.toObject(Activity.class);
                    selectedCategory.collection("activityList").document(currentActivity.getActivityId()).delete();
                }
            }
        });

        selectedCategory.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ActivitiesList.this, "Successfully deleted category & activities", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(ActivitiesList.this, "Error deleting category & activities", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpActivitiesList();
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