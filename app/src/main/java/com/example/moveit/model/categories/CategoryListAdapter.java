package com.example.moveit.model.categories;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.example.moveit.R;
import com.example.moveit.view.activities.ActivitiesList;

public class CategoryListAdapter extends ArrayAdapter<Category> {
    private final Context context;
    private final Integer resource;

    public CategoryListAdapter(Context context, Integer resource) {
        super(context, resource);
        this.context = context;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        String categoryName = getItem(position).getName();
        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("ViewHolder") View categoryView = inflater.inflate(resource, parent, false);

        TextView nameText = categoryView.findViewById(R.id.categoryNameTV);
        nameText.setText(categoryName);

        categoryView.setOnClickListener(v -> {
            Intent intent = ActivitiesList.makeIntent(context);
            intent.putExtra("categoryName", getItem(position).getName());
            intent.putExtra("categoryId", getItem(position).getCategoryId());
            context.startActivity(intent);
        });

        return categoryView;
    }
}
