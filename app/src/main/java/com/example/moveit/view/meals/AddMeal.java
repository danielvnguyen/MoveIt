package com.example.moveit.view.meals;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.moveit.R;
import com.example.moveit.model.Meal;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class AddMeal extends AppCompatActivity {

    private Meal currentMeal;
    private FirebaseAuth auth;
    private FirebaseFirestore database;
    private FirebaseUser currentUser;
    private EditText mealNameInput;
    private EditText caloriesInput;
    private EditText mealNotesInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_add_meal);

        database = FirebaseFirestore.getInstance();

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        assert currentUser != null;

        mealNameInput = findViewById(R.id.mealName);
        caloriesInput = findViewById(R.id.caloriesInput);
        mealNotesInput = findViewById(R.id.mealNotes);

        setUpSaveBtn();
    }

    private void setUpSaveBtn() {
        Button saveBtn = findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mealName = mealNameInput.getText().toString();
                Integer calories = Integer.parseInt(caloriesInput.getText().toString());
                String mealNotes = mealNotesInput.getText().toString();
                currentMeal = new Meal(mealName, calories, mealNotes);
                String mealId = mealName.replaceAll("\\s+","");

                database.collection("meals").document(currentUser.getUid()).collection("mealList")
                        .document(mealId).set(currentMeal).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(AddMeal.this, "Saved Meal Successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        }
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