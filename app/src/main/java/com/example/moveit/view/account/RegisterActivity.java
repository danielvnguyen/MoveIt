package com.example.moveit.view.account;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.TooltipCompat;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.moveit.R;
import com.example.moveit.model.PasswordValidator;
import com.example.moveit.model.activities.Activity;
import com.example.moveit.model.categories.Category;
import com.example.moveit.model.meals.Meal;
import com.example.moveit.model.meals.ServingSize;
import com.example.moveit.view.StartActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Objects;
import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {
    private EditText emailInput;
    private EditText passwordInput;
    private Button registerBtn;

    private FirebaseAuth auth;
    private PasswordValidator passwordValidator;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private final String[] defaultCategories = {"Strength Exercises", "Cardio Exercises", "Flexibility Exercises", "Balance Exercises"};
    private final String[][] defaultActivities = {{"Weightlifting", "Pull-ups", "Push-ups", "Planks", "Sit-ups"},
            {"Running", "Walking", "Swimming", "Skip Rope", "Cycling"}, {"Yoga", "Meditation", "Stretching"},
            {"High Knees", "Heel Taps", "Leg Raises", "Balance Walking"}};
    private final Meal[] defaultMeals = {
            new Meal(UUID.randomUUID().toString(), "Chicken Breast", 231,
                    new ServingSize(140, "g"), "1 cup, chopped or diced",
                    "https://cdn.pixabay.com/photo/2022/01/18/08/42/chicken-6946606_960_720.jpg"),
            new Meal(UUID.randomUUID().toString(), "White Rice", 130,
                    new ServingSize(100, "g"), "Cooked, long-grain",
                    "https://cdn.pixabay.com/photo/2012/11/27/03/35/usd-67411_960_720.jpg"),
            new Meal(UUID.randomUUID().toString(), "Chicken Caesar Salad", 309,
                    new ServingSize(192,"g"), "Includes 6.67g crouton, 5.56g parmesan",
                    "https://cdn.pixabay.com/photo/2014/01/17/08/56/caesar-246818_960_720.jpg"),
            new Meal(UUID.randomUUID().toString(), "California Roll", 28,
                    new ServingSize(30, "g"), "Non-spicy version",
                    "https://cdn.pixabay.com/photo/2021/02/25/20/45/california-shrimp-roll-6050079_960_720.jpg"),
            new Meal(UUID.randomUUID().toString(), "Quarter Pounder", 530,
                    new ServingSize(220, "g"), "One hamburger with single patty and no condiments",
                    "https://cdn.pixabay.com/photo/2017/04/04/16/25/hamburger-2201748_960_720.jpg")};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_register);
        setTitle(getString(R.string.register_title));

        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        registerBtn = findViewById(R.id.register);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        passwordValidator = new PasswordValidator();
        FloatingActionButton fab = findViewById(R.id.passwordToolTip);
        TooltipCompat.setTooltipText(fab, getString(R.string.password_rules));

        setUpRegisterBtn();
        setUpShowHideBtn();
    }

    private void setUpRegisterBtn() {
        registerBtn.setOnClickListener(view -> {
            String emailText = emailInput.getText().toString();
            String passwordText = passwordInput.getText().toString();

            if (TextUtils.isEmpty(emailText) || TextUtils.isEmpty(passwordText)) {
                Toast.makeText(RegisterActivity.this, "Email or Password is empty!", Toast.LENGTH_SHORT).show();
            } else if (!passwordValidator.validatePassword(passwordText)) {
                Toast.makeText(RegisterActivity.this, "Password is not strong enough!", Toast.LENGTH_SHORT).show();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                Toast.makeText(RegisterActivity.this, "Email has invalid format!", Toast.LENGTH_SHORT).show();
            } else {
                registerUser(emailText, passwordText);
            }
        });
    }

    private void setUpShowHideBtn() {
        Button showHideBtn = findViewById(R.id.showHideBtn);
        showHideBtn.setOnClickListener(v -> {
            if(showHideBtn.getText().toString().equals("Show")){
                passwordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                showHideBtn.setText(R.string.hide);
            } else{
                passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                showHideBtn.setText(R.string.show);
            }
        });
    }

    private void registerUser(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterActivity.this, task -> {
            if (task.isSuccessful()) {
                currentUser = auth.getCurrentUser();
                assert currentUser != null;
                initializeUser();
                currentUser.sendEmailVerification().addOnSuccessListener(unused -> {
                    auth.signOut();
                    Toast.makeText(RegisterActivity.this, "Email verification has been sent", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                });
            } else {
                Toast.makeText(RegisterActivity.this, "Failed to register", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeUser() {
        createDefaultCategories();
        createDefaultMeals();
    }

    private void createDefaultMeals() {
        for (Meal currentMeal : defaultMeals) {
            db.collection("meals").document(currentUser.getUid()).collection("mealList")
                    .document(currentMeal.getId()).set(currentMeal);
        }
    }

    private void createDefaultCategories() {
        for (int i = 0; i < defaultCategories.length; i++) {
            int index = i;
            String categoryId = UUID.randomUUID().toString();
            Category newCategory = new Category(defaultCategories[i], categoryId);
            db.collection("categories").document(currentUser.getUid()).collection("categoryList")
                    .document(categoryId).set(newCategory).addOnSuccessListener(unused -> createDefaultActivities(index, categoryId));
        }
    }

    private void createDefaultActivities(int position, String categoryId) {
        String[] currentActivities = defaultActivities[position];
        for (String currentActivity : currentActivities) {
            String activityId = UUID.randomUUID().toString();
            Activity newActivity = new Activity(currentActivity, categoryId, activityId);
            db.collection("categories").document(currentUser.getUid())
                    .collection("categoryList").document(categoryId)
                    .collection("activityList").document(activityId).set(newActivity);
        }
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, RegisterActivity.class);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, StartActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemID = item.getItemId();

        if (itemID == android.R.id.home) {
            Intent intent = new Intent(this, StartActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}