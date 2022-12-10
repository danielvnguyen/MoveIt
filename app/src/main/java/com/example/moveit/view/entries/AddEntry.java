package com.example.moveit.view.entries;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.moveit.R;
import com.example.moveit.model.activities.Activity;
import com.example.moveit.model.categories.Category;
import com.example.moveit.model.entries.Entry;
import com.example.moveit.model.meals.Meal;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressLint("SimpleDateFormat")
public class AddEntry extends AppCompatActivity implements
        TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private Button saveBtn;
    private Entry currentEntry;

    private TextView[] moodButtons;

    private EditText dateInput;
    private EditText timeInput;
    private Integer selectedHour, selectedMinute, selectedYear, selectedMonth, selectedDay;

    private ChipGroup mealChipGroup;
    private final Map<String, Integer> mealCaloriesMap = new HashMap<>();
    private Integer calorieSum;

    private ChipGroup activitiesChipGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_add_entry);
        setTitle(getString(R.string.add_entry_title));

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        currentEntry = new Entry();

        setUpMoods();
        setUpInterface();
        setUpMealChips();
        setUpActivities();
        //setUpSaveBtn();
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

                            Chip mealChip = buildChip(currentMeal.getName());
                            if (!currentMeal.getImageId().equals("")) {
                                if (currentMeal.getImageId().contains("https")) {
                                    loadChipIcon(mealChip, Uri.parse(currentMeal.getImageId()));
                                } else {
                                    final StorageReference fileRef = FirebaseStorage.getInstance()
                                            .getReference().child(currentUser.getUid())
                                            .child("uploads").child(currentMeal.getImageId());
                                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                        loadChipIcon(mealChip, uri);
                                    });
                                }
                            }

                            mealChip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                calorieSum = getCalorieSum();
                                currentEntry.setCaloriesEaten(calorieSum);
                                EditText calorieSumInput = findViewById(R.id.calorieSumInput);
                                calorieSumInput.setText(calorieSum.toString());
                            });
                            mealChipGroup.addView(mealChip);
                        }
                        progressDialog.dismiss();
                    } else {
                        Log.d("AddEntry", "Error retrieving meal documents: ", task.getException());
                    }
                });
    }

    private void setUpActivities() {
        activitiesChipGroup = findViewById(R.id.activitiesChipGroup);

        CollectionReference categoriesRef = db.collection("categories")
                .document(currentUser.getUid()).collection("categoryList");
        categoriesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot categoryDoc : Objects.requireNonNull(task.getResult())) {
                    Category currentCategory = categoryDoc.toObject(Category.class);
                    categoriesRef.document(currentCategory.getCategoryId())
                            .collection("activityList").get().addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    for (QueryDocumentSnapshot activityDoc : Objects.requireNonNull(task1.getResult())) {
                                        Activity currentActivity = activityDoc.toObject(Activity.class);
                                        Chip activityChip = buildChip(currentActivity.getName());
                                        activitiesChipGroup.addView(activityChip);
                                    }
                                } else {
                                    Log.d("AddEntry", "Error retrieving activity documents: ", task1.getException());
                                }
                            });
                }
            } else {
                Log.d("AddEntry", "Error retrieving category documents: ", task.getException());
            }
        });
    }

    private Chip buildChip(String text) {
        Chip newChip = new Chip(this);
        newChip.setText(text);
        newChip.setChipBackgroundColorResource(R.color.light_green);
        newChip.setCheckable(true);
        newChip.setChipIconVisible(true);

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

    public void showActivitiesChipGroup(View v) {
        LinearLayout activitiesChipLayout = findViewById(R.id.activitiesChipLayout);
        int visibility = (activitiesChipGroup.getVisibility() == View.GONE)? View.VISIBLE : View.GONE;
        TransitionManager.beginDelayedTransition(activitiesChipLayout, new AutoTransition());
        activitiesChipGroup.setVisibility(visibility);
    }

    private void setUpInterface() {
        dateInput = findViewById(R.id.entryDateInput);
        timeInput = findViewById(R.id.entryTimeInput);

        long currentTime = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy h:mm a");
        Date resultDate = new Date(currentTime);
        String dateTimeText = sdf.format(resultDate);

        String dateText = dateTimeText.substring(0, 13);
        String timeText = dateTimeText.substring(13);
        dateInput.setText(dateText);
        timeInput.setText(timeText);
    }

    private void setUpMoods() {
        TextView amazingMoodBtn = findViewById(R.id.amazingMoodBtn);
        TextView greatMoodBtn = findViewById(R.id.greatMoodBtn);
        TextView goodMoodBtn = findViewById(R.id.goodMoodBtn);
        TextView mehMoodBtn = findViewById(R.id.mehMoodBtn);
        TextView badMoodBtn = findViewById(R.id.badMoodBtn);
        moodButtons = new TextView[]{amazingMoodBtn, greatMoodBtn, goodMoodBtn, mehMoodBtn, badMoodBtn};

        for (TextView moodButton : moodButtons) {
            moodButton.setOnClickListener(v -> {
                currentEntry.setMood((String) moodButton.getText());
                moodButton.setTextColor(getResources().getColor(R.color.light_green));
                moodButton.setBackgroundResource(R.drawable.on_item_select);
                clearMoodSelections(moodButton);
            });
        }
    }

    private void clearMoodSelections(TextView currentButton) {
        for (TextView moodButton : moodButtons) {
            if (!moodButton.equals(currentButton)) {
                int color = MaterialColors.getColor(this, android.R.attr.textColor, Color.WHITE);
                moodButton.setTextColor(color);
                moodButton.setBackgroundResource(0);
            }
        }
    }

    public void showTimePickerDialog(View v) {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog.OnTimeSetListener timeSetListener = this;
        TimePickerDialog dialog = new TimePickerDialog(this, timeSetListener, hour, minute, false);
        if (selectedHour != null) {
            dialog.updateTime(selectedHour, selectedMinute);
        }
        dialog.show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        selectedHour = hourOfDay;
        selectedMinute = minute;
        String timeText = getTime(hourOfDay, minute);
        timeInput.setText(timeText);
    }

    private String getTime(int hr,int min) {
        Calendar c = Calendar.getInstance();
        c.set(0, 0, 0, selectedHour ,selectedMinute);
        currentEntry.setTime(c);
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
        return sdf.format(c.getTimeInMillis());
    }

    public void showDatePickerDialog(View v) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog.OnDateSetListener dateSetListener = this;
        DatePickerDialog dialog = new DatePickerDialog(this, dateSetListener, year, month, day);
        dialog.getDatePicker().setMaxDate(new Date().getTime());
        if (selectedYear != null) {
            dialog.updateDate(selectedYear, selectedMonth, selectedDay);
        }
        dialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        selectedYear = year;
        selectedMonth = month;
        selectedDay = day;
        String dateText = getDate(year, month, day);
        dateInput.setText(dateText);
    }

    private String getDate(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day);
        currentEntry.setDate(c);
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
}