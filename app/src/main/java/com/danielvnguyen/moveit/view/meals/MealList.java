package com.danielvnguyen.moveit.view.meals;

import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import com.danielvnguyen.moveit.R;
import com.danielvnguyen.moveit.model.meals.Meal;
import com.danielvnguyen.moveit.model.meals.MealListAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Objects;

public class MealList extends AppCompatActivity {

    private ArrayAdapter<Meal> adapter;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private EditText mealFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_meal_list);
        setTitle(getString(R.string.meal_list_title));

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        setUpAddMealBtn();
        setUpMealList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMealList();
    }

    private void setUpMealList() {
        ListView mealListView = findViewById(R.id.mealList);
        mealListView.setEmptyView(findViewById(R.id.emptyTV));
        mealFilter = findViewById(R.id.mealSearchFilter);

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        adapter = new MealListAdapter(MealList.this, R.layout.item_meal);
        mealListView.setAdapter(adapter);

        db.collection("meals").document(currentUser.getUid()).collection("mealList").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Meal> mealList = new ArrayList<>();
                        for (QueryDocumentSnapshot mealDoc : Objects.requireNonNull(task.getResult())) {
                            Meal currentMeal = mealDoc.toObject(Meal.class);
                            mealList.add(currentMeal);
                        }

                        adapter.clear();
                        adapter.addAll(mealList);
                        progressDialog.dismiss();
                        mealFilter.setVisibility(View.VISIBLE);
                        mealFilter.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                (MealList.this).adapter.getFilter().filter(s);
                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                            }
                        });
                    } else {
                        Log.d("MealList", "Error retrieving documents: ", task.getException());
                    }
                });
    }

    private void setUpAddMealBtn() {
        FloatingActionButton addMealBtn = findViewById(R.id.addMeal);
        addMealBtn.setOnClickListener(v -> startActivity(new Intent(MealList.this, AddMeal.class)));
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