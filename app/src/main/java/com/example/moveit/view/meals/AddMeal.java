package com.example.moveit.view.meals;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.moveit.R;
import com.example.moveit.model.meals.Meal;
import com.example.moveit.model.meals.ServingSize;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class AddMeal extends AppCompatActivity {

    private Meal currentMeal;

    private String originalMealId;
    private String originalMealImageId;
    private String originalMealName;
    private String originalMealCalories;
    private String originalMealNote;
    private String originalServingSizeNum;
    private String originalServingSizeUnits;
    private Boolean imageStateAltered = false;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseUser currentUser;

    private ImageView mealImageView;
    private Uri mealImageUri;
    private static final int IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private String mealImagePath;

    private ImageView chooseImgBtn;
    private ImageView deleteImgBtn;
    private ImageView takeNewImgBtn;

    private EditText mealNameInput;
    private EditText caloriesInput;
    private EditText mealNoteInput;
    private EditText servingSizeInput;
    private Spinner servingSizeUnitSpinner;
    private String selectedUnits = "Unit";

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
        setUpSpinner();
    }

    private void setUpDeleteBtn() {
        deleteBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle(R.string.confirm_delete_meal);
            builder.setMessage(R.string.no_takesies_backsies);
            builder.setPositiveButton(R.string.yes, (dialog, which) -> handleDelete());
            builder.setNegativeButton(R.string.no, (dialog, which) -> {});
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    private void handleDelete() {
        DocumentReference selectedMeal = db.collection("meals").document(currentUser.getUid()).collection("mealList")
                .document(originalMealId);

        if (!originalMealImageId.equals("")) {
            final StorageReference fileRef = storage.getReference().child(currentUser.getUid())
                    .child("uploads").child(originalMealImageId);
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
    }

    private void setUpInterface() {
        mealNameInput = findViewById(R.id.mealName);
        mealNameInput.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.required,0);
        caloriesInput = findViewById(R.id.caloriesInput);
        mealNoteInput = findViewById(R.id.mealNote);
        deleteBtn = findViewById(R.id.deleteMealBtn);
        chooseImgBtn = findViewById(R.id.chooseImageBtn);
        deleteImgBtn = findViewById(R.id.deleteImgBtn);
        takeNewImgBtn = findViewById(R.id.takeNewImageBtn);
        mealImageView = findViewById(R.id.mealImageView);
        servingSizeInput = findViewById(R.id.servingSizeInput);
        servingSizeUnitSpinner = findViewById(R.id.servingSizeSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.units_array));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        servingSizeUnitSpinner.setAdapter(adapter);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            setTitle(getString(R.string.edit_meal_title));
            deleteBtn.setVisibility(View.VISIBLE);
            editMode = true;

            originalMealId = extras.get("mealId").toString();
            originalMealImageId = extras.get("mealImageId").toString();
            originalMealName = extras.get("mealName").toString();
            originalMealCalories = extras.get("calories").toString();
            originalMealNote = extras.get("mealNote").toString();
            originalServingSizeNum = extras.get("servingSizeNum").toString();
            originalServingSizeUnits = extras.get("servingSizeUnits").toString();
            selectedUnits = originalServingSizeUnits;

            if (!originalMealImageId.equals("")) {
                if (originalMealImageId.contains("https")) {
                    Glide.with(mealImageView.getContext()).load(originalMealImageId).centerInside().into(mealImageView);
                    mealImageView.setVisibility(View.VISIBLE);
                    deleteImgBtn.setVisibility(View.VISIBLE);
                } else {
                    final StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(currentUser.getUid())
                            .child("uploads").child(originalMealImageId);
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Glide.with(mealImageView.getContext()).load(uri).centerInside().into(mealImageView);
                        mealImageView.setVisibility(View.VISIBLE);
                        deleteImgBtn.setVisibility(View.VISIBLE);
                    });
                }
            }
            mealNameInput.setText(originalMealName);
            caloriesInput.setText(originalMealCalories);
            mealNoteInput.setText(originalMealNote);
            servingSizeInput.setText(originalServingSizeNum);
            int spinnerPosition = adapter.getPosition(originalServingSizeUnits);
            servingSizeUnitSpinner.setSelection(spinnerPosition);
        } else {
            setTitle(getString(R.string.add_meal_title));
        }
    }

    private int validateCalories(String caloriesText, String servingSizeText) {
        if (!caloriesText.isEmpty() && (!servingSizeText.isEmpty() && !servingSizeText.equals("0")) && !selectedUnits.equals("Unit")) {
            return 0;
        } else if (!caloriesText.isEmpty() && (servingSizeText.isEmpty() || servingSizeText.equals("0")) && selectedUnits.equals("Unit")) {
            return 1;
        } else if (caloriesText.isEmpty() && (servingSizeText.isEmpty() || servingSizeText.equals("0")) && selectedUnits.equals("Unit")) {
            return 2;
        } else {
            Toast.makeText(AddMeal.this, "Please fill out both calories & serving size, or only calories", Toast.LENGTH_SHORT).show();
            return 3;
        }
    }

    private void setUpSaveBtn() {
        Button saveBtn = findViewById(R.id.saveMealBtn);
        saveBtn.setOnClickListener(v -> {
            String mealName = mealNameInput.getText().toString();
            String caloriesText = caloriesInput.getText().toString();
            String servingSizeText = servingSizeInput.getText().toString();
            String mealNote = mealNoteInput.getText().toString();
            if (mealName.isEmpty()) {
                Toast.makeText(AddMeal.this, "Please fill out the meal name", Toast.LENGTH_SHORT).show();
                return;
            }

            Integer calories = 0;
            Integer servingSizeNum = null;
            ServingSize servingSize = new ServingSize();
            int validationResult = validateCalories(caloriesText, servingSizeText);
            switch(validationResult) {
                case 0:
                    calories = Integer.parseInt(caloriesText);
                    servingSizeNum = Integer.parseInt(servingSizeText);
                    servingSize = new ServingSize(servingSizeNum, selectedUnits);
                    break;
                case 1:
                    calories = Integer.parseInt(caloriesText);
                    break;
                case 3:
                    return;
                default:
                    break;
            }

            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Saving Meal...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            if (editMode) {
                //Update existing entry
                if (compareChanges(mealName, calories, mealNote, servingSizeNum, selectedUnits)) {
                    Toast.makeText(AddMeal.this, "You have made no changes!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (imageStateAltered) {
                    if (mealImageUri != null) {
                        String imageId = UUID.randomUUID() + "." + getFileExtension(mealImageUri);
                        final StorageReference fileRef = storage.getReference().child(currentUser.getUid())
                                .child("uploads").child(imageId);
                        fileRef.putFile(mealImageUri);
                        if (originalMealImageId.equals("")) {
                            handleUpdate(mealName, calories, servingSize, mealNote, imageId);
                        } else {
                            final StorageReference oldImageRef = storage.getReference().child(currentUser.getUid())
                                    .child("uploads").child(originalMealImageId);
                            oldImageRef.delete();
                            handleUpdate(mealName, calories, servingSize, mealNote, imageId);
                        }
                    } else {
                        final StorageReference imageRef = storage.getReference().child(currentUser.getUid())
                                .child("uploads").child(originalMealImageId);
                        imageRef.delete();
                        handleUpdate(mealName, calories, servingSize, mealNote, "");
                    }
                } else {
                    handleUpdate(mealName, calories, servingSize, mealNote, originalMealImageId);
                }
                progressDialog.dismiss();
            } else {
                //Creating new entry
                String mealId = UUID.randomUUID().toString();
                if (mealImageUri != null) {
                    String imageId = UUID.randomUUID() + "." + getFileExtension(mealImageUri);
                    final StorageReference fileRef = storage.getReference().child(currentUser.getUid())
                            .child("uploads").child(imageId);
                    Integer finalCalories = calories;
                    ServingSize finalServingSize = servingSize;
                    fileRef.putFile(mealImageUri).addOnCompleteListener(task -> fileRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                        currentMeal = new Meal(mealId, mealName, finalCalories, finalServingSize, mealNote, imageId);
                        handleUpload(currentMeal);
                    }));
                } else {
                    currentMeal = new Meal(mealId, mealName, calories, servingSize, mealNote, "");
                    handleUpload(currentMeal);
                }
                progressDialog.dismiss();
            }
        });
    }

    private void setUpSpinner() {
        servingSizeUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedUnits = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setUpImageOptions() {
        chooseImgBtn.setOnClickListener(v -> selectImage());
        deleteImgBtn.setOnClickListener(v -> deleteImage());
        takeNewImgBtn.setOnClickListener(v -> takeNewImage());
    }

    private void takeNewImage() {
        if (checkAndRequestPermissions()) {
            startCameraIntent();
        }
    }

    private void deleteImage() {
        mealImageView.setVisibility(View.GONE);
        deleteImgBtn.setVisibility(View.GONE);
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
            mealImageUri = data.getData();
            Glide.with(mealImageView.getContext()).load(mealImageUri).centerInside().into(mealImageView);
            mealImageView.setVisibility(View.VISIBLE);
            deleteImgBtn.setVisibility(View.VISIBLE);
            imageStateAltered = true;
        } else if (requestCode == CAMERA_REQUEST
                && resultCode == RESULT_OK) {
            File f = new File(mealImagePath);
            mealImageUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", f);
            Glide.with(mealImageView.getContext()).load(mealImageUri).centerInside().into(mealImageView);
            mealImageView.setVisibility(View.VISIBLE);
            deleteImgBtn.setVisibility(View.VISIBLE);
            imageStateAltered = true;
        }
    }

    private void handleUpdate(String mealName, Integer calories, ServingSize servingSize, String mealNote, String imageId) {
        db.collection("meals").document(currentUser.getUid())
                .collection("mealList").document(originalMealId).update("name", mealName,
                "calories", calories, "servingSize", servingSize, "note", mealNote,
                        "imageId", imageId).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(AddMeal.this, "Updated meal successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(AddMeal.this, "Error updating meal", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Boolean compareChanges(String mealName, Integer calories, String mealNote,
                                   Integer servingSizeNum, String servingSizeUnits ) {
        return originalMealName.equals(mealName) && originalMealCalories.equals(String.valueOf(calories))
                && originalMealNote.equals(mealNote) && !imageStateAltered && originalServingSizeNum.equals(String.valueOf(servingSizeNum))
                && originalServingSizeUnits.equals(servingSizeUnits);
    }

    private void handleUpload(Meal meal) {
        db.collection("meals").document(currentUser.getUid()).collection("mealList")
                .document(meal.getId()).set(meal).addOnCompleteListener(task -> {
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

    private void startCameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File imageFile = null;
            try {
                imageFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (imageFile != null) {
                Uri imageUri = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider", imageFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private File createImageFile() throws IOException {
        String currentTimeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageName = "jpg_"+currentTimeStamp+"_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(imageName, ".jpg", storageDir);

        mealImagePath = imageFile.getAbsolutePath();
        return imageFile;
    }

    private boolean checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= 27) {
            int cameraPermission = ActivityCompat.checkSelfPermission(
                    AddMeal.this, Manifest.permission.CAMERA);
            if (cameraPermission == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(AddMeal.this,
                        new String[]{Manifest.permission.CAMERA}, 20);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 20 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCameraIntent();
        } else
            Toast.makeText(AddMeal.this, "Permission required to take photos",
                    Toast.LENGTH_SHORT).show();
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