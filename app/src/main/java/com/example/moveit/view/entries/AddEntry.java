package com.example.moveit.view.entries;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.moveit.R;
import com.example.moveit.model.TimePickerFragment;
import com.example.moveit.model.entries.Entry;
import com.example.moveit.model.meals.Meal;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.color.MaterialColors;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.sql.Time;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

@SuppressLint("SimpleDateFormat")
public class AddEntry extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private Button saveBtn;
    private Entry currentEntry;

    private EditText dateInput;
    private EditText timeInput;
    private int hour, minute;
    private TextView[] moodButtons;

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
        //setUpSaveBtn();
    }

    @SuppressWarnings("Convert2MethodRef")
    private void setUpMealChips() {
        ChipGroup mealChipGroup = findViewById(R.id.mealChipGroup);

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading...");
        progressDialog.show();

        db.collection("meals").document(currentUser.getUid()).collection("mealList").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot mealDoc : Objects.requireNonNull(task.getResult())) {
                            Meal currentMeal = mealDoc.toObject(Meal.class);
                            Chip currentChip = buildChip(currentMeal.getName());
                            if (!currentMeal.getImageId().equals("")) {
                                if (currentMeal.getImageId().contains("https")) {
                                    loadChipIcon(currentChip, Uri.parse(currentMeal.getImageId()));
                                } else {
                                    final StorageReference fileRef = FirebaseStorage.getInstance()
                                            .getReference().child(currentUser.getUid())
                                            .child("uploads").child(currentMeal.getImageId());
                                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                        loadChipIcon(currentChip, uri);
                                        progressDialog.dismiss();
                                    });
                                }
                            }

                            mealChipGroup.addView(currentChip);
                            mealChipGroup.setOnCheckedChangeListener((group, checkedId)
                                    -> group.check(checkedId));
                        }
                    } else {
                        Log.d("MealList", "Error retrieving documents: ", task.getException());
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

    private void loadChipIcon(Chip chip, Uri uri) {
        Glide.with(this).load(uri).circleCrop().listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                chip.setChipIcon(resource);
                return false;
            }
        }).preload();
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

        amazingMoodBtn.setOnClickListener(v -> {
            currentEntry.setMood("AMAZING");
            amazingMoodBtn.setTextColor(getResources().getColor(R.color.light_green));
            clearMoodSelections(amazingMoodBtn);
        });
        greatMoodBtn.setOnClickListener(v -> {
            currentEntry.setMood("GREAT");
            greatMoodBtn.setTextColor(getResources().getColor(R.color.light_green));
            clearMoodSelections(greatMoodBtn);
        });
        goodMoodBtn.setOnClickListener(v -> {
            currentEntry.setMood("GOOD");
            goodMoodBtn.setTextColor(getResources().getColor(R.color.light_green));
            clearMoodSelections(goodMoodBtn);
        });
        mehMoodBtn.setOnClickListener(v -> {
            currentEntry.setMood("MEH");
            mehMoodBtn.setTextColor(getResources().getColor(R.color.light_green));
            clearMoodSelections(mehMoodBtn);
        });
        badMoodBtn.setOnClickListener(v -> {
            currentEntry.setMood("BAD");
            badMoodBtn.setTextColor(getResources().getColor(R.color.light_green));
            clearMoodSelections(badMoodBtn);
        });
    }

    private void clearMoodSelections(TextView currentButton) {
        for (TextView moodButton : moodButtons) {
            if (!moodButton.equals(currentButton)) {
                int color = MaterialColors.getColor(this, android.R.attr.textColor, Color.WHITE);
                moodButton.setTextColor(color);
            }
        }
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
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

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String timeText = getTime(hourOfDay, minute);
        timeInput.setText(timeText);
        currentEntry.setTime(timeText);
    }

    private String getTime(int hr,int min) {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
        Date resultDate = new Date(0, 0, 0, hr, min);
        return sdf.format(resultDate);
    }
}