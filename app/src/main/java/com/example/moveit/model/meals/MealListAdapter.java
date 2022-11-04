package com.example.moveit.model.meals;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.example.moveit.R;
import com.example.moveit.view.meals.AddMeal;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Objects;

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
        String mealName = getItem(position).getName();
        String mealImageId = getItem(position).getImageId();
        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("ViewHolder") View mealView = inflater.inflate(resource, parent, false);

        TextView nameText = mealView.findViewById(R.id.mealNameTV);
        nameText.setText(mealName);
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        ImageView mealImageView = mealView.findViewById(R.id.mealImageView);
        if (!mealImageId.equals("")) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            final StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(currentUser.getUid())
                    .child("uploads").child(mealImageId);
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String url = uri.toString();
                Picasso.with(mealImageView.getContext()).load(url).fit().into(mealImageView);
                mealImageView.setVisibility(View.VISIBLE);
            });
        }

        mealView.setOnClickListener(v -> {
           Intent intent = AddMeal.makeIntent(context);
           intent.putExtra("editMode", true);
           intent.putExtra("mealName", getItem(position).getName());
           intent.putExtra("calories", getItem(position).getCalories());
           intent.putExtra("mealNote", getItem(position).getNote());
           intent.putExtra("mealId", getItem(position).getId(userId));
           intent.putExtra("mealImageId", getItem(position).getImageId());
           context.startActivity(intent);
        });
        return mealView;
    }
}
