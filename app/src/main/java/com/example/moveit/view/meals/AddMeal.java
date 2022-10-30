package com.example.moveit.view.meals;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.moveit.R;
import com.example.moveit.model.meals.Meal;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.Objects;

public class AddMeal extends AppCompatActivity {

    private Meal currentMeal;
    private String originalMealId;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private FirebaseStorage storage;
    private StorageReference reference;

    private ImageView mealImageView;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 22;

    private ImageView chooseImgBtn;
    private ImageView uploadImgBtn;
    private ImageView deleteImgBtn;

    private EditText mealNameInput;
    private EditText caloriesInput;
    private EditText mealNotesInput;

    private Button deleteBtn;
    private Boolean editMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_add_meal);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        reference = storage.getReference();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        assert currentUser != null;

        setUpInterface();
        setUpSaveBtn();
        setUpDeleteBtn();
        setUpImageOptions();
    }

    private void setUpImageOptions() {
        chooseImgBtn.setOnClickListener(v -> selectImage());
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select image from here:"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                        getContentResolver(), filePath);
                mealImageView = findViewById(R.id.mealImageView);
                mealImageView.setImageBitmap(bitmap);
                mealImageView.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setUpDeleteBtn() {
        deleteBtn.setOnClickListener(v -> {
            DocumentReference selectedMeal = db.collection("meals").document(currentUser.getUid()).collection("mealList")
                    .document(originalMealId);

            selectedMeal.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(AddMeal.this, "Deleted meal successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddMeal.this, "Error deleting meal", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void setUpInterface() {
        mealNameInput = findViewById(R.id.mealName);
        mealNameInput.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.required,0);
        caloriesInput = findViewById(R.id.caloriesInput);
        mealNotesInput = findViewById(R.id.mealNotes);
        deleteBtn = findViewById(R.id.deleteBtn);
        chooseImgBtn = findViewById(R.id.chooseImageBtn);
        uploadImgBtn = findViewById(R.id.uploadImgBtn);
        deleteImgBtn = findViewById(R.id.deleteImgBtn);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            setTitle("Editing Meal");
            deleteBtn.setVisibility(View.VISIBLE);

            editMode = (Boolean) extras.get("editMode");
            originalMealId = extras.get("mealId").toString();

            String originalName = extras.get("mealName").toString();
            String originalCalories = extras.get("calories").toString();
            String originalNote = extras.get("mealNote").toString();
            mealNameInput.setText(originalName);
            caloriesInput.setText(originalCalories);
            mealNotesInput.setText(originalNote);
        } else {
            setTitle("Adding New Meal");
        }
    }

    private void setUpSaveBtn() {
        Button saveBtn = findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(v -> {
            String mealName = mealNameInput.getText().toString();
            if (mealName.equals("")) {
                Toast.makeText(AddMeal.this, "Please fill out the meal name", Toast.LENGTH_SHORT).show();
                return;
            }
            Integer calories = Integer.parseInt(caloriesInput.getText().toString());
            String mealNotes = mealNotesInput.getText().toString();
            currentMeal = new Meal(mealName, calories, mealNotes);
            String mealId = mealName.replaceAll("\\s+","");

            if (editMode) {
                db.collection("meals").document(currentUser.getUid()).collection("mealList")
                        .document(originalMealId).delete();
                db.collection("meals").document(currentUser.getUid()).collection("mealList")
                        .document(mealId).set(currentMeal).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(AddMeal.this, "Updated meal successfully!", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(AddMeal.this, "Error updating meal", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                db.collection("meals").document(currentUser.getUid()).collection("mealList")
                        .document(mealId).set(currentMeal).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(AddMeal.this, "Saved meal successfully!", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(AddMeal.this, "Error saving meal", Toast.LENGTH_SHORT).show();
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

    public static Intent makeIntent(Context context) {
        return new Intent(context, AddMeal.class);
    }
}