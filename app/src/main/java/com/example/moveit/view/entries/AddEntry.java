package com.example.moveit.view.entries;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.moveit.R;
import com.example.moveit.model.activities.Activity;
import com.example.moveit.model.categories.Category;
import com.example.moveit.model.entries.Entry;
import com.example.moveit.model.meals.Meal;
import com.example.moveit.view.HomeActivity;
import com.example.moveit.view.meals.AddMeal;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.color.MaterialColors;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@SuppressLint("SimpleDateFormat")
public class AddEntry extends AppCompatActivity implements
        TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private FirebaseStorage storage;

    private Button saveEntryBtn;
    private Button deleteEntryBtn;
    private Entry currentEntry;

    private final Map<String, TextView> moodButtonsMap = new HashMap<>();
    private EditText entryNote;

    private ImageView entryImageView;
    private Uri entryImageUri;
    private ImageView chooseImgBtn;
    private ImageView deleteImgBtn;
    private ImageView takeNewImgBtn;
    private static final int IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private String entryImagePath;

    private EditText dateInput;
    private EditText timeInput;
    private Integer selectedHour, selectedMinute, selectedYear, selectedMonth, selectedDay;
    private long dateTimeValue;

//    private ChipGroup activitiesChipGroup;
    //list of Category (strings) that contain their own array of activities
    private LinearLayout categoryLinearLayout;
    private ChipGroup mealChipGroup;
    private final Map<String, Integer> mealCaloriesMap = new HashMap<>();
    private EditText caloriesInput;

    private Boolean editMode = false;
    private Boolean imageStateAltered = false;
    private String originalEntryId, originalEntryImageId, originalEntryCalories,
            originalEntryNote, originalMood;
    private ArrayList<String> originalMeals, originalActivities;
    private long originalDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_add_entry);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();

        currentEntry = new Entry();

        setUpMoods();
        setUpInterface();
        setUpMealChips();
//        setUpActivities();
        setUpImageOptions();
        setUpSaveBtn();
        setUpDeleteBtn();
        setUpCategories();
    }

    private void setUpSaveBtn() {
        saveEntryBtn.setOnClickListener(v -> {
            if (currentEntry.getMood().equals("")) {
                Toast.makeText(AddEntry.this, "Please select a mood for the Entry", Toast.LENGTH_SHORT).show();
                return;
            }
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Saving Entry...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            String entryImageId;
            if (!editMode) {
                //Creating new entry
                entryImageId = "";
                if (entryImageUri != null) {
                    entryImageId = UUID.randomUUID() + "." + getFileExtension(entryImageUri);
                    storage.getReference().child(currentUser.getUid()).child("uploads")
                            .child(entryImageId).putFile(entryImageUri);
                }

                currentEntry.setCaloriesEaten(Integer.valueOf(caloriesInput.getText().toString()));
                currentEntry.setNote(entryNote.getText().toString());
                currentEntry.setDateTime(dateTimeValue);
                currentEntry.setImageId(entryImageId);
                currentEntry.setMeals(getSelectedMeals());
//                currentEntry.setActivities(getSelectedActivities());

                String entryId = UUID.randomUUID().toString();
                currentEntry.setId(entryId);
                handleUpload(currentEntry);
            } else {
                currentEntry.setCaloriesEaten(Integer.valueOf(caloriesInput.getText().toString()));
                currentEntry.setNote(entryNote.getText().toString());
                currentEntry.setDateTime(dateTimeValue);
                currentEntry.setMeals(getSelectedMeals());
//                currentEntry.setActivities(getSelectedActivities());

                //Updating existing entry
                if (compareChanges()) {
                    Toast.makeText(AddEntry.this, "You have made no changes!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    return;
                }

                entryImageId = originalEntryImageId;
                currentEntry.setImageId(entryImageId);
                if (imageStateAltered) {
                    if (entryImageUri != null) {
                        entryImageId = UUID.randomUUID() + "." + getFileExtension(entryImageUri);
                        currentEntry.setImageId(entryImageId);

                        storage.getReference().child(currentUser.getUid()).child("uploads")
                                .child(entryImageId).putFile(entryImageUri);
                        if (originalEntryImageId.equals("")) {
                            handleUpdate(currentEntry, entryImageId);
                        } else {
                            final StorageReference oldEntryImageRef = storage.getReference().child(currentUser.getUid())
                                    .child("uploads").child(originalEntryImageId);
                            oldEntryImageRef.delete();
                            handleUpdate(currentEntry, entryImageId);
                        }
                    } else {
                        final StorageReference oldEntryImageRef = storage.getReference().child(currentUser.getUid())
                                .child("uploads").child(originalEntryImageId);
                        oldEntryImageRef.delete();
                        handleUpdate(currentEntry, "");
                    }
                } else {
                    handleUpdate(currentEntry, originalEntryImageId);
                }
            }
            progressDialog.dismiss();
        });
    }

    private void handleUpdate(Entry entry, String imageId) {
        db.collection("entries").document(currentUser.getUid()).collection("entryList").document(originalEntryId)
                .update("mood", entry.getMood(), "meals", entry.getMeals(), "activities", entry.getActivities(),
                        "caloriesEaten", entry.getCaloriesEaten(), "dateTime", entry.getDateTime(), "note", entry.getNote(),
                        "imageId", imageId).addOnCompleteListener(task -> {
           if (task.isSuccessful()) {
               Toast.makeText(AddEntry.this, "Updated entry successfully!", Toast.LENGTH_SHORT).show();
               Intent intent = new Intent(this, HomeActivity.class);
               intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
               intent.putExtra("isChanged", true);
               startActivity(intent);
           } else {
               Toast.makeText(AddEntry.this, "Error updating entry", Toast.LENGTH_SHORT).show();
           }
        });
    }

    private boolean compareChanges() {
        return (currentEntry.getMood().equals(originalMood) &&
                String.valueOf(currentEntry.getCaloriesEaten()).equals(originalEntryCalories)
                && currentEntry.getNote().equals(originalEntryNote) && !imageStateAltered
                && currentEntry.getDateTime() == originalDateTime
                && currentEntry.getMeals().equals(originalMeals)
                && currentEntry.getActivities().equals(originalActivities));
    }

    private void handleUpload(Entry entry) {
        db.collection("entries").document(currentUser.getUid()).collection("entryList")
                .document(entry.getId()).set(entry).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AddEntry.this, "Saved entry successfully!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.putExtra("isChanged", true);
                        startActivity(intent);
                    } else {
                        Toast.makeText(AddEntry.this, "Error saving entry", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setUpDeleteBtn() {
        deleteEntryBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle(R.string.confirm_delete_entry);
            builder.setMessage(R.string.no_takesies_backsies);
            builder.setPositiveButton(R.string.yes, (dialog, which) -> handleDelete());
            builder.setNegativeButton(R.string.no, (dialog, which) -> {});
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    private void handleDelete() {
        if (!originalEntryImageId.equals("")) {
            final StorageReference fileRef = storage.getReference().child(currentUser.getUid())
                    .child("uploads").child(originalEntryImageId);
            fileRef.delete();
        }

        db.collection("entries").document(currentUser.getUid()).collection("entryList")
                .document(originalEntryId).delete().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AddEntry.this, "Deleted entry successfully!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.putExtra("isChanged", true);
                        startActivity(intent);
                    } else {
                        Toast.makeText(AddEntry.this, "Error deleting entry", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setUpInterface() {
        saveEntryBtn = findViewById(R.id.saveEntryBtn);
        deleteEntryBtn = findViewById(R.id.deleteEntryBtn);
        dateInput = findViewById(R.id.entryDateInput);
        timeInput = findViewById(R.id.entryTimeInput);
        entryNote = findViewById(R.id.entryNote);
        entryImageView = findViewById(R.id.entryImageView);
        chooseImgBtn = findViewById(R.id.chooseImageBtn);
        deleteImgBtn = findViewById(R.id.deleteImgBtn);
        takeNewImgBtn = findViewById(R.id.takeNewImageBtn);
        caloriesInput = findViewById(R.id.calorieSumInput);

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            setTitle(getString(R.string.editing_entry_title));
            deleteEntryBtn.setVisibility(View.VISIBLE);
            editMode = true;

            originalEntryId = extras.get("entryId").toString();
            originalEntryImageId = extras.get("imageId").toString();
            originalEntryCalories = extras.get("caloriesEaten").toString();
            originalDateTime = (long) extras.get("dateTimeValue");
            originalEntryNote = extras.get("entryNote").toString();
            originalMood = extras.get("entryMood").toString();
            originalMeals = (ArrayList<String>) extras.get("selectedMeals");
            originalActivities = (ArrayList<String>) extras.get("selectedActivities");

            if (!originalEntryImageId.equals("")) {
                final StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(currentUser.getUid())
                        .child("uploads").child(originalEntryImageId);
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Glide.with(entryImageView.getContext()).load(uri).centerInside().into(entryImageView);
                    entryImageView.setVisibility(View.VISIBLE);
                    deleteImgBtn.setVisibility(View.VISIBLE);
                });
            }

            caloriesInput.setText(originalEntryCalories);
            entryNote.setText(originalEntryNote);
            dateTimeValue = originalDateTime;
            setDateTime(dateTimeValue);
            Calendar originalCalendar = Calendar.getInstance();
            originalCalendar.setTimeInMillis(originalDateTime);
            selectedYear = originalCalendar.get(Calendar.YEAR);
            selectedMonth = originalCalendar.get(Calendar.MONTH);
            selectedDay = originalCalendar.get(Calendar.DAY_OF_MONTH);
            selectedHour = originalCalendar.get(Calendar.HOUR_OF_DAY);
            selectedMinute = originalCalendar.get(Calendar.MINUTE);
            selectMood(Objects.requireNonNull(moodButtonsMap.get(originalMood)));

        } else {
            setTitle(getString(R.string.add_entry_title));
            getCurrentDateTime();
            setDateTime(dateTimeValue);
        }
        progressDialog.dismiss();
    }

    private void setDateTime(long dateTimeMilliseconds) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy h:mm a");
        Date resultDate = new Date(dateTimeMilliseconds);
        String dateTimeText = sdf.format(resultDate);

        String dateText = dateTimeText.substring(0, 13);
        String timeText = dateTimeText.substring(13);
        dateInput.setText(dateText);
        timeInput.setText(timeText);
    }

    private void getCurrentDateTime() {
        Calendar currentCalendar = Calendar.getInstance();
        selectedYear = currentCalendar.get(Calendar.YEAR);
        selectedMonth = currentCalendar.get(Calendar.MONTH);
        selectedDay = currentCalendar.get(Calendar.DAY_OF_MONTH);
        selectedHour = currentCalendar.get(Calendar.HOUR_OF_DAY);
        selectedMinute = currentCalendar.get(Calendar.MINUTE);
        currentCalendar.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute, 0);

        dateTimeValue = currentCalendar.getTimeInMillis();
    }

    private void setUpImageOptions() {
        chooseImgBtn.setOnClickListener(v -> selectImage());
        takeNewImgBtn.setOnClickListener(v -> takeNewImage());
        deleteImgBtn.setOnClickListener(v -> deleteImage());
    }

    private void takeNewImage() {
        if (checkAndRequestPermissions()) {
            startCameraIntent();
        }
    }

    private void deleteImage() {
        entryImageView.setVisibility(View.GONE);
        deleteImgBtn.setVisibility(View.GONE);
        imageStateAltered = true;
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select image from here:"), IMAGE_REQUEST);
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

    private String getFileExtension(Uri imageUri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri));
    }

    private File createImageFile() throws IOException {
        String currentTimeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageName = "jpg_"+currentTimeStamp+"_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(imageName, ".jpg", storageDir);

        entryImagePath = imageFile.getAbsolutePath();
        return imageFile;
    }

    private boolean checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= 27) {
            int cameraPermission = ActivityCompat.checkSelfPermission(
                    AddEntry.this, Manifest.permission.CAMERA);
            if (cameraPermission == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(AddEntry.this,
                        new String[]{Manifest.permission.CAMERA}, 20);
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {
            entryImageUri = data.getData();
            Glide.with(entryImageView.getContext()).load(entryImageUri).centerInside().into(entryImageView);
            entryImageView.setVisibility(View.VISIBLE);
            deleteImgBtn.setVisibility(View.VISIBLE);
            imageStateAltered = true;
        } else if (requestCode == CAMERA_REQUEST
                && resultCode == RESULT_OK) {
            File f = new File(entryImagePath);
            entryImageUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", f);
            Glide.with(entryImageView.getContext()).load(entryImageUri).centerInside().into(entryImageView);
            entryImageView.setVisibility(View.VISIBLE);
            deleteImgBtn.setVisibility(View.VISIBLE);
            imageStateAltered = true;
        }
    }

    @SuppressLint("SetTextI18n")
    private void setUpMealChips() {
        mealChipGroup = findViewById(R.id.mealChipGroup);

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        db.collection("meals").document(currentUser.getUid()).collection("mealList").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot mealDoc : Objects.requireNonNull(task.getResult())) {
                            Meal currentMeal = mealDoc.toObject(Meal.class);
                            mealCaloriesMap.put(currentMeal.getName(), currentMeal.getCalories());

                            Chip mealChip = buildChip(currentMeal.getName(), "Meal");
                            if (!currentMeal.getImageId().equals("")) {
                                if (currentMeal.getImageId().contains("https")) {
                                    loadChipIcon(mealChip, Uri.parse(currentMeal.getImageId()));
                                } else {
                                    final StorageReference fileRef = FirebaseStorage.getInstance()
                                            .getReference().child(currentUser.getUid())
                                            .child("uploads").child(currentMeal.getImageId());
                                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> loadChipIcon(mealChip, uri));
                                }
                            }

                            mealChip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                EditText calorieSumInput = findViewById(R.id.calorieSumInput);
                                calorieSumInput.setText(getCalorieSum().toString());
                            });
                            mealChipGroup.addView(mealChip);
                        }
                        progressDialog.dismiss();
                    } else {
                        Log.d("AddEntry", "Error retrieving meal documents: ", task.getException());
                    }
                });
    }

    private void setUpCategories() {
        this.categoryLinearLayout = findViewById(R.id.categoryLinearLayout);

        CollectionReference categoriesRef = db.collection("categories")
                .document(currentUser.getUid()).collection("categoryList");
        categoriesRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot categoryDoc : Objects.requireNonNull(task.getResult())) {
                        Category currentCategory = categoryDoc.toObject(Category.class);
                        String currentCategoryId = currentCategory.getCategoryId();
                        String currentCategoryName = currentCategory.getName();
                        //Create CardView for each category with its name
                        CardView cardView = new CardView(this);
                        LinearLayout linearLayout = new LinearLayout(this);
                        linearLayout.setOrientation(LinearLayout.VERTICAL);
                        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));

                        //TextView styling
                        TextView cardViewLabel = new TextView(this);
                        Typeface ralewayTypeface = getResources().getFont(R.font.raleway);
                        cardViewLabel.setTypeface(ralewayTypeface);
                        cardViewLabel.setText(currentCategoryName);
                        cardViewLabel.setTextSize(16);
                        linearLayout.addView(cardViewLabel);

                        //CardView styling
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        int cardViewMargin = dpToPx(10);
                        layoutParams.setMargins(cardViewMargin, cardViewMargin, cardViewMargin, cardViewMargin);
                        cardView.setLayoutParams(layoutParams);
                        cardView.setRadius(dpToPx(20));
                        cardView.setCardElevation(dpToPx(10));
                        cardView.setContentPadding(dpToPx(15), dpToPx(15), dpToPx(15), dpToPx(15));
                        cardView.addView(linearLayout);

                        //set on click listener for card view

                        categoryLinearLayout.addView(cardView);
                    }
                } else {
                    Log.d("AddEntry", "Error retrieving category documents: ", task.getException());
                }
            });

    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

//    private void setUpActivities() {
//        activitiesChipGroup = findViewById(R.id.activitiesChipGroup);
//
//        CollectionReference categoriesRef = db.collection("categories")
//                .document(currentUser.getUid()).collection("categoryList");
//        categoriesRef.get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                for (QueryDocumentSnapshot categoryDoc : Objects.requireNonNull(task.getResult())) {
//                    Category currentCategory = categoryDoc.toObject(Category.class);
//                    categoriesRef.document(currentCategory.getCategoryId())
//                            .collection("activityList").get().addOnCompleteListener(task1 -> {
//                                if (task1.isSuccessful()) {
//                                    for (QueryDocumentSnapshot activityDoc : Objects.requireNonNull(task1.getResult())) {
//                                        Activity currentActivity = activityDoc.toObject(Activity.class);
//                                        Chip activityChip = buildChip(currentActivity.getName(), "Activity");
//                                        activitiesChipGroup.addView(activityChip);
//                                    }
//                                } else {
//                                    Log.d("AddEntry", "Error retrieving activity documents: ", task1.getException());
//                                }
//                            });
//                }
//            } else {
//                Log.d("AddEntry", "Error retrieving category documents: ", task.getException());
//            }
//        });
//    }

    private Chip buildChip(String text, String type) {
        Chip newChip = new Chip(this);
        newChip.setText(text);
        newChip.setTextColor(getResources().getColor(R.color.white));
        newChip.setChipBackgroundColorResource(R.color.light_green);
        newChip.setCheckable(true);
        newChip.setChipIconVisible(true);

        if (editMode) {
            if (type.equals("Meal") && originalMeals.contains(text)) {
                newChip.setChecked(true);
            } else if (type.equals("Activity") && originalActivities.contains(text)) {
                newChip.setChecked(true);
            }
        }

        return newChip;
    }

    private Integer getCalorieSum() {
        List<Integer> ids = mealChipGroup.getCheckedChipIds();
        Integer calories = 0;
        for (Integer id: ids){
            Chip currentChip = mealChipGroup.findViewById(id);
            calories += mealCaloriesMap.get(currentChip.getText().toString());
        }
        return calories;
    }

    private ArrayList<String> getSelectedMeals() {
        List<Integer> ids = mealChipGroup.getCheckedChipIds();
        ArrayList<String> selectedMeals = new ArrayList<>();
        for (Integer id: ids){
            Chip currentChip = mealChipGroup.findViewById(id);
            selectedMeals.add(currentChip.getText().toString());
        }
        return selectedMeals;
    }

//    private ArrayList<String> getSelectedActivities() {
//        List<Integer> ids = activitiesChipGroup.getCheckedChipIds();
//        ArrayList<String> selectedActivities = new ArrayList<>();
//        for (Integer id: ids){
//            Chip currentChip = activitiesChipGroup.findViewById(id);
//            selectedActivities.add(currentChip.getText().toString());
//        }
//        return selectedActivities;
//    }

    private void loadChipIcon(Chip chip, Uri uri) {
        Glide.with(this).asBitmap().load(uri).circleCrop().into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                Drawable d = new BitmapDrawable(getResources(), resource);
                chip.setChipIcon(d);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
            }
        });
    }

    public void showMealChipGroup(View v) {
        LinearLayout mealChipLayout = findViewById(R.id.mealChipLayout);
        int visibility = (mealChipGroup.getVisibility() == View.GONE)? View.VISIBLE : View.GONE;
        TransitionManager.beginDelayedTransition(mealChipLayout, new AutoTransition());
        mealChipGroup.setVisibility(visibility);
    }

//    public void showActivitiesChipGroup(View v) {
//        LinearLayout activitiesChipLayout = findViewById(R.id.activitiesChipLayout);
//        int visibility = (activitiesChipGroup.getVisibility() == View.GONE)? View.VISIBLE : View.GONE;
//        TransitionManager.beginDelayedTransition(activitiesChipLayout, new AutoTransition());
//        activitiesChipGroup.setVisibility(visibility);
//    }

    private void setUpMoods() {
        TextView amazingMoodBtn = findViewById(R.id.amazingMoodBtn);
        TextView greatMoodBtn = findViewById(R.id.greatMoodBtn);
        TextView goodMoodBtn = findViewById(R.id.goodMoodBtn);
        TextView mehMoodBtn = findViewById(R.id.mehMoodBtn);
        TextView badMoodBtn = findViewById(R.id.badMoodBtn);
        TextView[] moodButtonTextViews = new TextView[]{amazingMoodBtn, greatMoodBtn, goodMoodBtn, mehMoodBtn, badMoodBtn};

        for (TextView moodButton : moodButtonTextViews) {
            moodButtonsMap.put(String.valueOf(moodButton.getText()), moodButton);
            moodButton.setOnClickListener(v -> selectMood(moodButton));
        }
    }

    private void selectMood(TextView moodButton) {
        currentEntry.setMood((String) moodButton.getText());
        moodButton.setTextColor(getResources().getColor(R.color.light_green));
        moodButton.setBackgroundResource(R.drawable.on_item_select);
        clearMoodSelections(moodButton);
    }

    private void clearMoodSelections(TextView currentButton) {
        for (TextView moodButton : moodButtonsMap.values()) {
            if (!moodButton.equals(currentButton)) {
                int color = MaterialColors.getColor(this, android.R.attr.textColor, Color.WHITE);
                moodButton.setTextColor(color);
                moodButton.setBackgroundResource(0);
            }
        }
    }

    //Time and date functions

    public void showTimePickerDialog(View v) {
        TimePickerDialog.OnTimeSetListener timeSetListener = this;
        TimePickerDialog dialog = new TimePickerDialog(this, timeSetListener, selectedHour, selectedMinute, false);
        dialog.show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        selectedHour = hourOfDay;
        selectedMinute = minute;
        String timeText = formatTime();
        timeInput.setText(timeText);
    }

    private String formatTime() {
        Calendar c = Calendar.getInstance();
        c.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute, 0);
        dateTimeValue = c.getTimeInMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
        return sdf.format(c.getTimeInMillis());
    }

    public void showDatePickerDialog(View v) {
        DatePickerDialog.OnDateSetListener dateSetListener = this;
        DatePickerDialog dialog = new DatePickerDialog(this, dateSetListener, selectedYear, selectedMonth, selectedDay);
        dialog.getDatePicker().setMaxDate(new Date().getTime());
        dialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        selectedYear = year;
        selectedMonth = month;
        selectedDay = day;
        String dateText = formatDate(year, month, day);
        dateInput.setText(dateText);
    }

    private String formatDate(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day, selectedHour, selectedMinute, 0);
        dateTimeValue = c.getTimeInMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
        return sdf.format(c.getTimeInMillis());
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
        return new Intent(context, AddEntry.class);
    }
}