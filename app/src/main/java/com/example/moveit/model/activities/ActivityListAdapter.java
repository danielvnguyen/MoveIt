package com.example.moveit.model.activities;

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
import com.example.moveit.view.activities.AddActivity;

public class ActivityListAdapter extends ArrayAdapter<CategoryActivity> {
    private final Context context;
    private final Integer resource;

    public ActivityListAdapter(Context context, Integer resource) {
        super(context, resource);
        this.context = context;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        String activityName = getItem(position).getName();
        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("ViewHolder") View activityView = inflater.inflate(resource, parent, false);

        TextView nameText = activityView.findViewById(R.id.activityNameTV);
        nameText.setText(activityName);

        activityView.setOnClickListener(v -> {
            Intent intent = AddActivity.makeIntent(context);
            intent.putExtra("activityName", getItem(position).getName());
            intent.putExtra("activityId", getItem(position).getActivityId());
            intent.putExtra("categoryId", getItem(position).getCategoryId());
            context.startActivity(intent);
        });

        return activityView;
    }
}
