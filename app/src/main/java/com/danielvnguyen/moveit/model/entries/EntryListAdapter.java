package com.danielvnguyen.moveit.model.entries;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.danielvnguyen.moveit.R;
import com.danielvnguyen.moveit.model.meals.Meal;
import com.danielvnguyen.moveit.view.entries.AddEntry;
import com.danielvnguyen.moveit.view.photoGallery.ViewPhotoActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressLint("SimpleDateFormat")
public class EntryListAdapter extends ArrayAdapter<Entry> {
    private final Context context;
    private final Integer resource;
    private final FirebaseFirestore db;
    private final FirebaseUser currentUser;
    private final Map<String, Integer[]> moodResourcesMap = new HashMap<>();

    public EntryListAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        this.context = context;
        this.resource = resource;

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        //Setting up resources to get corresponding drawable/colour values
        String[] moods = {"Amazing", "Great", "Good", "Meh", "Bad"};
        List<Integer[]> allResources = new ArrayList<>();
        allResources.add(new Integer[]{R.drawable.amazing_icon, R.color.amazingColour});
        allResources.add(new Integer[]{R.drawable.great_icon, R.color.greatColour});
        allResources.add(new Integer[]{R.drawable.good_icon, R.color.goodColour});
        allResources.add(new Integer[]{R.drawable.meh_icon, R.color.mehColour});
        allResources.add(new Integer[]{R.drawable.bad_icon, R.color.badColour});

        for (int i = 0; i < moods.length; i++) {
            moodResourcesMap.put(moods[i], allResources.get(i));
        }
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("ViewHolder") View entryView = inflater.inflate(resource, parent, false);

        ImageView moodImage = entryView.findViewById(R.id.moodImageView);
        TextView dateTimeText = entryView.findViewById(R.id.entryDateTimeTV);
        TextView moodText = entryView.findViewById(R.id.moodTV);
        ChipGroup mealChipGroup = entryView.findViewById(R.id.mealChipGroup);
        ChipGroup activitiesChipGroup = entryView.findViewById(R.id.activitiesChipGroup);
        TextView entryNotesText = entryView.findViewById(R.id.entryNotesTv);
        TextView entryNotesHeader = entryView.findViewById(R.id.entryNoteHeader);
        ImageView entryImageView = entryView.findViewById(R.id.entryImageView);
        TextView mealChipGroupHeader = entryView.findViewById(R.id.mealChipGroupHeader);
        TextView activitiesChipGroupHeader = entryView.findViewById(R.id.activitiesChipGroupHeader);
        TextView caloriesHeader = entryView.findViewById(R.id.caloriesHeaderTV);
        TextView caloriesValue = entryView.findViewById(R.id.caloriesValueTV);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy, h:mm a");

        Entry currentEntry = getItem(position);
        String selectedMood = currentEntry.getMood();
        moodImage.setImageResource(Objects.requireNonNull(moodResourcesMap.get(selectedMood))[0]);
        moodImage.setColorFilter(ContextCompat.getColor(context,
                Objects.requireNonNull(moodResourcesMap.get(selectedMood))[1]));
        dateTimeText.setText(sdf.format(currentEntry.getDateTime()));
        moodText.setText(currentEntry.getMood());
        moodText.setTextColor(context.getResources().getColor(Objects.requireNonNull(moodResourcesMap.get(selectedMood))[1]));
        if (!(currentEntry.getCaloriesEaten() == 0)) {
            caloriesHeader.setVisibility(View.VISIBLE);
            caloriesValue.setVisibility(View.VISIBLE);
            caloriesValue.setText(currentEntry.getCaloriesEaten().toString());
        }
        if (!currentEntry.getNote().equals("")) {
            entryNotesText.setText(currentEntry.getNote());
            entryNotesText.setVisibility(View.VISIBLE);
            entryNotesHeader.setVisibility(View.VISIBLE);
        }
        if (!currentEntry.getImageId().equals("")) {
            final StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(currentUser.getUid())
                    .child("uploads").child(currentEntry.getImageId());
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(entryImageView.getContext()).load(uri).centerCrop().into(entryImageView);
                entryImageView.setVisibility(View.VISIBLE);
            });

            entryImageView.setOnClickListener(v -> {
                Intent intent = ViewPhotoActivity.makeIntent(context);
                intent.putExtra("imageId", currentEntry.getImageId());
                intent.putExtra("showDelete", false);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                context.startActivity(intent);
            });
        }
        ArrayList<String> selectedMeals = currentEntry.getMeals();
        if (!selectedMeals.isEmpty()) {
            mealChipGroupHeader.setVisibility(View.VISIBLE);
            for (String mealName : selectedMeals) {
                Chip mealChip = buildMealChip(mealName);
                getMealImageId(mealName, mealChip);
                mealChipGroup.addView(mealChip);
            }
        }
        HashMap<String, ArrayList<String>> selectedActivities = currentEntry.getActivities();
        if (!selectedActivities.isEmpty()) {
            activitiesChipGroupHeader.setVisibility(View.VISIBLE);
            for (String activityName : selectedActivities.keySet()) {
                int count = Objects.requireNonNull(selectedActivities.get(activityName)).size();
                Chip activityChip = buildActivityChip(activityName, count);
                activitiesChipGroup.addView(activityChip);
            }
        }

        entryView.setOnClickListener(v -> {
            Intent intent = AddEntry.makeIntent(context);
            intent.putExtra("entryId", getItem(position).getId());
            intent.putExtra("entryMood", getItem(position).getMood());
            intent.putExtra("selectedMeals", getItem(position).getMeals());
            intent.putExtra("selectedActivities", getItem(position).getActivities());
            intent.putExtra("caloriesEaten", getItem(position).getCaloriesEaten());
            intent.putExtra("dateTimeValue", getItem(position).getDateTime());
            intent.putExtra("entryNote", getItem(position).getNote());
            intent.putExtra("imageId", getItem(position).getImageId());
            context.startActivity(intent);
        });

        return entryView;
    }

    private void getMealImageId(String mealName, Chip mealChip) {
        db.collection("meals").document(currentUser.getUid()).collection("mealList")
                .whereEqualTo("name", mealName).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot mealDoc : task.getResult()) {
                            Meal currentMeal = mealDoc.toObject(Meal.class);
                            String currentImageId = currentMeal.getImageId();
                            if (!currentImageId.equals("")) {
                                if (currentImageId.contains("https")) {
                                    loadChipIcon(mealChip, Uri.parse(currentImageId));
                                } else {
                                    final StorageReference fileRef = FirebaseStorage.getInstance()
                                            .getReference().child(currentUser.getUid())
                                            .child("uploads").child(currentImageId);
                                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> loadChipIcon(mealChip, uri));
                                }
                            }
                        }
                    }
                });
    }

    private Chip buildMealChip(String mealName) {
        Chip newChip = new Chip(context);
        newChip.setText(mealName);
        newChip.setTextColor(context.getResources().getColor(R.color.white));
        newChip.setChipIconVisible(true);
        newChip.setChipBackgroundColorResource(R.color.light_green);

        return newChip;
    }

    @SuppressLint("SetTextI18n")
    private Chip buildActivityChip(String activityName, int count) {
        Chip newChip = new Chip(context);
        if (count > 1) {
            newChip.setText(activityName + " (" + count + ")");
        } else {
            newChip.setText(activityName);
        }
        newChip.setTextColor(context.getResources().getColor(R.color.white));
        newChip.setChipIconVisible(true);
        newChip.setChipBackgroundColorResource(R.color.light_green);

        return newChip;
    }

    private void loadChipIcon(Chip chip, Uri uri) {
        Glide.with(context).asBitmap().load(uri).circleCrop().into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                Drawable d = new BitmapDrawable(context.getResources(), resource);
                chip.setChipIcon(d);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
            }
        });
    }

}
