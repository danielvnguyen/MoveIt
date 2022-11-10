package com.example.moveit.view.meals;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
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
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class AddMeal extends AppCompatActivity {

    private String mealId;
    private Meal currentMeal;
    private Boolean imageStateAltered = false;

    private String originalImageId;
    private String originalName;
    private String originalCalories;
    private String originalNote;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseUser currentUser;

    private ImageView mealImageView;
    private Uri imageUri;
    private final int IMAGE_REQUEST = 1;

    private ImageView chooseImgBtn;
    private ImageView deleteImgBtn;

    private EditText mealNameInput;
    private EditText caloriesInput;
    private EditText mealNoteInput;

    private Button deleteBtn;
    private Boolean editMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_add_meal);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

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
        deleteImgBtn.setOnClickListener(v -> deleteImage());
    }

    private void deleteImage() {
        mealImageView.setVisibility(View.INVISIBLE);
        deleteImgBtn.setVisibility(View.INVISIBLE);
        imageStateAltered = true;
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select image from here:"), IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                        getContentResolver(), imageUri);
                mealImageView.setImageBitmap(bitmap);
                mealImageView.setVisibility(View.VISIBLE);
                deleteImgBtn.setVisibility(View.VISIBLE);
                imageStateAltered = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setUpDeleteBtn() {
        deleteBtn.setOnClickListener(v -> {
            DocumentReference selectedMeal = db.collection("meals").document(currentUser.getUid()).collection("mealList")
                    .document(mealId);

            if (!originalImageId.equals("")) {
                final StorageReference fileRef = storage.getReference().child(currentUser.getUid())
                        .child("uploads").child(originalImageId);
                fileRef.delete();
            }
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
        mealNoteInput = findViewById(R.id.mealNote);
        deleteBtn = findViewById(R.id.deleteBtn);
        chooseImgBtn = findViewById(R.id.chooseImageBtn);
        deleteImgBtn = findViewById(R.id.deleteImgBtn);
        mealImageView = findViewById(R.id.mealImageView);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            setTitle(getString(R.string.edit_meal_title));
            deleteBtn.setVisibility(View.VISIBLE);

            editMode = (Boolean) extras.get("editMode");
            mealId = extras.get("mealId").toString();
            originalImageId = extras.get("mealImageId").toString();

            originalName = extras.get("mealName").toString();
            originalCalories = extras.get("calories").toString();
            originalNote = extras.get("mealNote").toString();

            if (!originalImageId.equals("")) {
                final StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(currentUser.getUid())
                        .child("uploads").child(originalImageId);
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String url = uri.toString();
                    Picasso.with(mealImageView.getContext()).load(url).fit().into(mealImageView);
                    mealImageView.setVisibility(View.VISIBLE);
                    deleteImgBtn.setVisibility(View.VISIBLE);
                });
            }
            mealNameInput.setText(originalName);
            caloriesInput.setText(originalCalories);
            mealNoteInput.setText(originalNote);
        } else {
            setTitle(getString(R.string.add_meal_title));
        }
    }

    private void setUpSaveBtn() {
        Button saveBtn = findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(v -> {
            String mealName = mealNameInput.getText().toString();
            Integer calories = Integer.parseInt(caloriesInput.getText().toString());
            String mealNote = mealNoteInput.getText().toString();
            if (mealName.equals("")) {
                Toast.makeText(AddMeal.this, "Please fill out the meal name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (editMode) {
                if (compareChanges(mealName, calories, mealNote, imageStateAltered)) {
                    Toast.makeText(AddMeal.this, "You have made no changes!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (imageStateAltered) {
                    if (imageUri != null) {
                        String imageId = UUID.randomUUID() + "." + getFileExtension(imageUri);
                        final StorageReference fileRef = storage.getReference().child(currentUser.getUid())
                                .child("uploads").child(imageId);
                        fileRef.putFile(imageUri);
                        if (originalImageId.equals("")) {
                            handleUpdate(mealName, calories, mealNote, imageId);
                        } else {
                            final StorageReference oldImageRef = storage.getReference().child(currentUser.getUid())
                                    .child("uploads").child(originalImageId);
                            oldImageRef.delete();
                            handleUpdate(mealName, calories, mealNote, imageId);
                        }
                    } else {
                        final StorageReference imageRef = storage.getReference().child(currentUser.getUid())
                                .child("uploads").child(originalImageId);
                        imageRef.delete();
                        handleUpdate(mealName, calories, mealNote, "");
                    }
                } else {
                    handleUpdate(mealName, calories, mealNote, originalImageId);
                }
            } else {
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Saving...");
                progressDialog.show();

                String mealId = UUID.randomUUID().toString();
                if (imageUri != null) {
                    String imageId = UUID.randomUUID() + "." + getFileExtension(imageUri);
                    final StorageReference fileRef = storage.getReference().child(currentUser.getUid())
                            .child("uploads").child(imageId);
                    fileRef.putFile(imageUri).addOnCompleteListener(task -> fileRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                        currentMeal = new Meal(mealId, mealName, calories, mealNote, imageId);
                        handleUpload(currentMeal, mealId);
                        progressDialog.dismiss();
                    }));
                } else {
                    currentMeal = new Meal(mealId, mealName, calories, mealNote, "");
                    handleUpload(currentMeal, mealId);
                    progressDialog.dismiss();
                }
            }
        });
    }

    //imageId: will be a valid image Id or an empty string ("") depending on the situation
    private void handleUpdate(String mealName, Integer calories, String mealNote, String imageId) {
        db.collection("meals").document(currentUser.getUid())
                .collection("mealList").document(mealId).update("name", mealName,
                "calories", calories, "note", mealNote, "imageId", imageId).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(AddMeal.this, "Updated meal successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(AddMeal.this, "Error updating meal", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Boolean compareChanges(String mealName, Integer calories, String mealNote, Boolean imageStateAltered) {
        return originalName.equals(mealName) && originalCalories.equals(String.valueOf(calories))
                && originalNote.equals(mealNote) && !imageStateAltered;
    }

    private void handleUpload(Meal meal, String mealId) {
        db.collection("meals").document(currentUser.getUid()).collection("mealList")
                .document(mealId).set(meal).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(AddMeal.this, "Saved meal successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(AddMeal.this, "Error saving meal", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFileExtension(Uri imageUri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri));
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