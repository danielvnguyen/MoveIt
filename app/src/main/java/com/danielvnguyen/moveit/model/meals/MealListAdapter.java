package com.danielvnguyen.moveit.model.meals;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.bumptech.glide.Glide;
import com.danielvnguyen.moveit.R;
import com.danielvnguyen.moveit.view.meals.AddMeal;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MealListAdapter extends ArrayAdapter<Meal> {
    private final Context context;
    private final Integer resource;

    public MealListAdapter(Context context, Integer resource) {
        super(context, resource);
        this.context = context;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("ViewHolder") View mealView = inflater.inflate(resource, parent, false);
        TextView nameText = mealView.findViewById(R.id.mealNameTV);
        TextView caloriesText = mealView.findViewById(R.id.mealCaloriesTV);

        String mealName = getItem(position).getName();
        String mealImageId = getItem(position).getImageId();

        nameText.setText(mealName);
        if (getItem(position).getCalories() != null) {
            if (!getItem(position).getServingSize().getUnits().equals("Unit")) {
                String mealCaloriesServingSize = (getItem(position).getCalories() + " calories per " +
                        getItem(position).getServingSize().getSize() + getItem(position).getServingSize().getUnits());
                caloriesText.setText(mealCaloriesServingSize);
            } else {
                String mealCalories = (getItem(position).getCalories() + " calories");
                caloriesText.setText(mealCalories);
            }
        } else {
            caloriesText.setText("");
        }

        ImageView mealImageView = mealView.findViewById(R.id.mealImageView);
        if (!mealImageId.equals("")) {
            if (mealImageId.contains("https")) {
                Glide.with(mealImageView.getContext()).load(mealImageId).centerCrop().into(mealImageView);
                mealImageView.setVisibility(View.VISIBLE);
            } else {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                assert currentUser != null;
                final StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(currentUser.getUid())
                        .child("uploads").child(mealImageId);
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Glide.with(mealImageView.getContext()).load(uri).centerCrop().into(mealImageView);
                    mealImageView.setVisibility(View.VISIBLE);
                });
            }
        }

        mealView.setOnClickListener(v -> {
           Intent intent = AddMeal.makeIntent(context);
           intent.putExtra("mealName", getItem(position).getName());
           intent.putExtra("calories", getItem(position).getCalories());
           intent.putExtra("servingSizeNum", getItem(position).getServingSize().getSize());
           intent.putExtra("servingSizeUnits", getItem(position).getServingSize().getUnits());
           intent.putExtra("mealNote", getItem(position).getNote());
           intent.putExtra("mealId", getItem(position).getId());
           intent.putExtra("mealImageId", getItem(position).getImageId());
           context.startActivity(intent);
        });
        return mealView;
    }
}
