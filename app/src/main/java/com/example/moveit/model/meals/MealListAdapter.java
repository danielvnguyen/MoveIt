package com.example.moveit.model.meals;

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
import com.example.moveit.R;
import com.example.moveit.view.meals.AddMeal;
import com.google.firebase.auth.FirebaseAuth;
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
        String mealImageUrl = getItem(position).getImage();
        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("ViewHolder") View mealView = inflater.inflate(resource, parent, false);

        TextView nameText = mealView.findViewById(R.id.mealNameTV);
        nameText.setText(mealName);
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        ImageView mealImageView = mealView.findViewById(R.id.mealImageView);
        if (!mealImageUrl.equals("")) {
            Picasso.with(mealImageView.getContext()).load(mealImageUrl).fit().into(mealImageView);
            mealImageView.setVisibility(View.VISIBLE);
        }

        mealView.setOnClickListener(v -> {
           Intent intent = AddMeal.makeIntent(context);
           intent.putExtra("editMode", true);
           intent.putExtra("mealName", getItem(position).getName());
           intent.putExtra("calories", getItem(position).getCalories());
           intent.putExtra("mealNote", getItem(position).getNote());
           intent.putExtra("mealId", getItem(position).getId(userId));
           intent.putExtra("mealImageUrl", getItem(position).getImage());
           context.startActivity(intent);
        });
        return mealView;
    }
}
