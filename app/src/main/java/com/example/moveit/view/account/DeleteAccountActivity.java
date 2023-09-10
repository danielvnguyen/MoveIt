package com.example.moveit.view.account;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.moveit.R;
import com.example.moveit.model.activities.CategoryActivity;
import com.example.moveit.model.categories.Category;
import com.example.moveit.model.entries.Entry;
import com.example.moveit.model.meals.Meal;
import com.example.moveit.view.StartActivity;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.Objects;

public class DeleteAccountActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private String currentUserId;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private EditText passwordInput;
    private Button showHideBtn;

    private boolean isGoogleSignInOnly = false;
    private boolean googleAccountVerified = false;
    private GoogleSignInClient signInClient;
    private static final String ID_TOKEN = "446715183529-ucspush1pj4sqs89s71ipeeoooq476e5.apps.googleusercontent.com";
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.delete_account));

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        assert currentUser != null;
        currentUserId = currentUser.getUid();
        storage = FirebaseStorage.getInstance();
        passwordInput = findViewById(R.id.passwordInput);
        showHideBtn = findViewById(R.id.passwordShowHideBtn);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isGoogleSignInOnly = extras.getBoolean("isGoogleSignInOnly");
        }
        if (isGoogleSignInOnly) {
            SignInButton signInButton = findViewById(R.id.googleSignInBtn);
            TextView googleSignInPrompt = findViewById(R.id.googleSignInPrompt);
            GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(ID_TOKEN)
                    .requestEmail()
                    .build();
            signInClient = GoogleSignIn.getClient(this, signInOptions);

            passwordInput.setVisibility(View.GONE);
            showHideBtn.setVisibility(View.GONE);
            signInButton.setSize(SignInButton.SIZE_WIDE);
            googleSignInPrompt.setVisibility(View.VISIBLE);
            signInButton.setVisibility(View.VISIBLE);
            signInButton.setOnClickListener(view -> {
                signInClient.signOut();
                Intent intent = signInClient.getSignInIntent();
                startActivityForResult(intent, RC_SIGN_IN);
            });
        } else {
            setUpShowHideBtn();
        }
        setUpDeleteBtn();
    }

    private void setUpDeleteBtn() {
        Button deleteBtn = findViewById(R.id.deleteButton);
        deleteBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle(R.string.confirm_delete_account);
            builder.setMessage(R.string.no_takesies_backsies);
            builder.setPositiveButton(R.string.yes, (dialog, which) -> handleDeleteAccount());
            builder.setNegativeButton(R.string.no, (dialog, which) -> {});
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    private void handleDeleteAccount() {
        if (isGoogleSignInOnly) {
            if (googleAccountVerified) {
                deleteUserData(currentUserId);
            } else {
                Toast.makeText(DeleteAccountActivity.this, "Please log in to your Google account", Toast.LENGTH_SHORT).show();
            }
        } else {
            String passwordText = passwordInput.getText().toString();
            if (TextUtils.isEmpty(passwordText)) {
                Toast.makeText(DeleteAccountActivity.this, "Please enter your password", Toast.LENGTH_SHORT).show();
            } else {
                auth.signInWithEmailAndPassword(Objects.requireNonNull(currentUser.getEmail()), passwordText).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        deleteUserData(currentUserId);
                    } else {
                        Toast.makeText(DeleteAccountActivity.this, "Password is incorrect", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
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
            currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    currentUser.reauthenticate(credential).addOnSuccessListener(unused -> {
                                googleAccountVerified = true;
                                Toast.makeText(DeleteAccountActivity.this, "Log in successful", Toast.LENGTH_SHORT).show();
                            }).addOnFailureListener(e ->
                            Log.d("DeleteAccountActivity", "Failed to authenticate user: ", e));
                } else {
                    Toast.makeText(DeleteAccountActivity.this, "Incorrect account", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (ApiException e) {
            Log.d("LoginActivity", "Failed to log in", e);
        }
    }

    private void deleteUserData(String userId) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Deleting User...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        CollectionReference entriesRef = db.collection("entries").document(userId).collection("entryList");
        CollectionReference categoriesRef = db.collection("categories").document(userId).collection("categoryList");
        CollectionReference mealsRef = db.collection("meals").document(userId).collection("mealList");
        CollectionReference reminderRef = db.collection("reminders").document(userId).collection("reminderTime");
        StorageReference storageRef = storage.getReference().child(userId).child("uploads");

        reminderRef.document("reminder").delete();

        //Entries:
        entriesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<String> entryIds = new ArrayList<>();
                for (QueryDocumentSnapshot entryDoc : Objects.requireNonNull(task.getResult())) {
                    Entry currentEntry = entryDoc.toObject(Entry.class);
                    entryIds.add(currentEntry.getId());
                }
                WriteBatch batch = db.batch();
                for (String entryId : entryIds) {
                    batch.delete(entriesRef.document(entryId));
                }
                batch.commit()
                        .addOnSuccessListener((result) -> Log.i("DeleteAccountActivity", "Entries have been removed."))
                        .addOnFailureListener((error) -> Log.e("DeleteAccountActivity", "Failed to remove entries.", error));
            }
        });

        //Meals:
        mealsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<String> mealIds = new ArrayList<>();
                for (QueryDocumentSnapshot mealDoc : Objects.requireNonNull(task.getResult())) {
                    Meal currentMeal = mealDoc.toObject(Meal.class);
                    mealIds.add(currentMeal.getId());
                }
                WriteBatch batch = db.batch();
                for (String mealId : mealIds) {
                    batch.delete(mealsRef.document(mealId));
                }
                batch.commit()
                        .addOnSuccessListener((result) -> Log.i("DeleteAccountActivity", "Meals have been removed."))
                        .addOnFailureListener((error) -> Log.e("DeleteAccountActivity", "Failed to remove meals.", error));
            }
        });

        //Images:
        storageRef.listAll().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (int i = 0; i < Objects.requireNonNull(task.getResult()).getItems().size(); i++) {
                    String fileName = task.getResult().getItems().get(i).getName();
                    storageRef.child(fileName).delete();
                }
            }
        });

        //Categories:
        categoriesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<String> categoryIds = new ArrayList<>();
                for (QueryDocumentSnapshot categoryDoc : Objects.requireNonNull(task.getResult())) {
                    Category currentCategory = categoryDoc.toObject(Category.class);
                    categoryIds.add(currentCategory.getCategoryId());
                }

                //Delete activities within each category, as well as the category
                for (String categoryId : categoryIds) {
                    categoriesRef.document(categoryId).collection("activityList").get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            ArrayList<CategoryActivity> activities = new ArrayList<>();
                            for (QueryDocumentSnapshot activityDoc : Objects.requireNonNull(task1.getResult())) {
                                CategoryActivity currentActivity = activityDoc.toObject(CategoryActivity.class);
                                activities.add(currentActivity);
                            }
                            WriteBatch batch = db.batch();
                            for (CategoryActivity activity : activities) {
                                if (activity.getCategoryId().equals(categoryId)) {
                                    batch.delete(categoriesRef.document(categoryId).collection("activityList").document(activity.getActivityId()));
                                }
                            }
                            batch.delete(categoriesRef.document(categoryId));
                            batch.commit()
                                    .addOnSuccessListener((result) -> Log.i("DeleteAccountActivity", "Activities have been removed for category: " + categoryId))
                                    .addOnFailureListener((error) -> Log.e("DeleteAccountActivity", "Failed to remove activities for category: " + categoryId, error));
                        }
                    });
                }

                //Delete user
                currentUser.delete().addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful()) {
                        progressDialog.dismiss();
                        Intent intent = StartActivity.makeIntent(DeleteAccountActivity.this);
                        finishAffinity();
                        startActivity(intent);
                        Toast.makeText(DeleteAccountActivity.this, "Your account has been deleted", Toast.LENGTH_SHORT).show();

                        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(ID_TOKEN)
                                .requestEmail()
                                .build();
                        GoogleSignInClient signInClient = GoogleSignIn.getClient(this, signInOptions);
                        signInClient.signOut();
                    }
                });
            }
        });
    }

    private void setUpShowHideBtn() {
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

    public static Intent makeIntent(Context context) {
        return new Intent(context, DeleteAccountActivity.class);
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