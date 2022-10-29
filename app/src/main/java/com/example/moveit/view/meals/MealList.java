package com.example.moveit.view.meals;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.moveit.R;
import com.example.moveit.model.meals.Meal;
import com.example.moveit.model.meals.MealListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class MealList extends AppCompatActivity {

    private FloatingActionButton addMealBtn;
    private ArrayAdapter<Meal> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_meal_list);
        setTitle(getString(R.string.your_meals));

        addMealBtn = findViewById(R.id.addMeal);

        setUpMealList();
        setUpAddMealBtn();
    }

    protected void onStart() {
        super.onStart();
        adapter.notifyDataSetChanged();
    }

    private void setUpMealList() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        ListView mealListView = findViewById(R.id.mealList);
        adapter = new MealListAdapter(MealList.this, R.layout.item_meal);
        mealListView.setAdapter(adapter);

        db.collection("meals").document(currentUser.getUid()).collection("mealList").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<Meal> mealList = new ArrayList<>();
                            for (QueryDocumentSnapshot mealDoc : Objects.requireNonNull(task.getResult())) {
                                Meal currentMeal = mealDoc.toObject(Meal.class);
                                mealList.add(currentMeal);
                            }

                            adapter.clear();
                            adapter.addAll(mealList);
                        } else {
                            Log.d("MealList", "Error retrieving documents: ", task.getException());
                        }
                    }
                });
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