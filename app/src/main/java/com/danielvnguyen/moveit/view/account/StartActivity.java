package com.danielvnguyen.moveit.view.account;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import com.danielvnguyen.moveit.R;
import com.danielvnguyen.moveit.model.activities.CategoryActivity;
import com.danielvnguyen.moveit.model.categories.Category;
import com.danielvnguyen.moveit.model.meals.Meal;
import com.danielvnguyen.moveit.model.meals.ServingSize;
import com.danielvnguyen.moveit.model.theme.ThemeSharedPreferences;
import com.danielvnguyen.moveit.view.homepage.HomeActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Objects;
import java.util.UUID;

public class StartActivity extends AppCompatActivity {
    private ThemeSharedPreferences preferencesManager;
    private FirebaseUser currentUser;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private GoogleSignInClient signInClient;
    private static final int RC_SIGN_IN = 9001;
    private static final String ID_TOKEN = "446715183529-ucspush1pj4sqs89s71ipeeoooq476e5.apps.googleusercontent.com";

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
        setContentView(R.layout.activity_start);
        preferencesManager = new ThemeSharedPreferences(StartActivity.this);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setUpButtons();
        setUpGoogleSignInBtn();
    }

    private void setUpGoogleSignInBtn() {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(ID_TOKEN)
                .requestEmail()
                .build();
        signInClient = GoogleSignIn.getClient(this, signInOptions);

        SignInButton googleSignInBtn = findViewById(R.id.googleSignInBtn);
        googleSignInBtn.setOnClickListener(view -> {
            Intent intent = signInClient.getSignInIntent();
            startActivityForResult(intent, RC_SIGN_IN);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignIn(accountTask);
        }
    }

    private void handleGoogleSignIn(Task<GoogleSignInAccount> accountTask) {
        try {
            GoogleSignInAccount account = accountTask.getResult(ApiException.class);
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            auth.signInWithCredential(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (Objects.requireNonNull(task.getResult().getAdditionalUserInfo()).isNewUser()) {
                        //Initialize user only if this is their first time signing in
                        initializeUser(Objects.requireNonNull(auth.getCurrentUser()).getUid());
                    }
                    setUpTheme();
                    Toast.makeText(StartActivity.this, "Log in successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(StartActivity.this, HomeActivity.class));
                    finish();
                } else {
                    Toast.makeText(StartActivity.this, "Failed to log in", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (ApiException e) {
            Log.d("LoginActivity", "Failed to log in", e);
        }
    }

    private void setUpTheme() {
        int currentTheme = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);

        if (currentUser != null) {
            if (preferencesManager.getValue(currentUser.getUid() + ".currentTheme", "Dark").equals("Dark")){
                switch (currentTheme) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        recreate();
                        break;
                }
            } else if (preferencesManager.getValue(currentUser.getUid() + ".currentTheme", "Light").equals("Light")) {
                switch (currentTheme) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        recreate();
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                        break;
                }
            }
        }
    }

    private void setUpButtons() {
        Button registerBtn = findViewById(R.id.register);
        Button loginBtn = findViewById(R.id.login);

        registerBtn.setOnClickListener((v) -> {
            Intent intent = RegisterActivity.makeIntent(this);
            startActivity(intent);
        });

        loginBtn.setOnClickListener((v) -> {
            Intent intent = LoginActivity.makeIntent(this);
            startActivity(intent);
        });
    }

    private void initializeUser(String userUid) {
        createDefaultCategories(userUid);
        createDefaultMeals(userUid);
    }

    private void createDefaultMeals(String userUid) {
        for (Meal currentMeal : defaultMeals) {
            db.collection("meals").document(userUid).collection("mealList")
                    .document(currentMeal.getId()).set(currentMeal);
        }
    }

    private void createDefaultCategories(String userUid) {
        for (int i = 0; i < defaultCategories.length; i++) {
            int index = i;
            String categoryId = UUID.randomUUID().toString();
            Category newCategory = new Category(defaultCategories[i], categoryId);
            db.collection("categories").document(userUid).collection("categoryList")
                    .document(categoryId).set(newCategory).addOnSuccessListener(unused -> createDefaultActivities(index, categoryId, userUid));
        }
    }

    private void createDefaultActivities(int position, String categoryId, String userUid) {
        String[] currentActivities = defaultActivities[position];
        for (String currentActivity : currentActivities) {
            String activityId = UUID.randomUUID().toString();
            CategoryActivity newActivity = new CategoryActivity(currentActivity, categoryId, activityId, "");
            db.collection("categories").document(userUid)
                    .collection("categoryList").document(categoryId)
                    .collection("activityList").document(activityId).set(newActivity);
        }
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, StartActivity.class);
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        setUpTheme();
        if (currentUser != null) {
            startActivity(new Intent(StartActivity.this, HomeActivity.class));
        }
    }
}