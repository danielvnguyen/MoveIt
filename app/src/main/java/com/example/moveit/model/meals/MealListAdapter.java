package com.example.moveit.model.meals;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.example.moveit.R;

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
        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("ViewHolder") View mealView = inflater.inflate(resource, parent, false);

        TextView nameText = mealView.findViewById(R.id.mealNameTV);
        nameText.setText(mealName);

        return mealView;
    }
}
