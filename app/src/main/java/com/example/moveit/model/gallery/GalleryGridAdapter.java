package com.example.moveit.model.gallery;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.moveit.R;
import com.google.firebase.storage.StorageReference;
import java.util.List;

public class GalleryGridAdapter extends BaseAdapter {
    private final Context context;
    private final List<StorageReference> images;
    private LayoutInflater inflater;

    public GalleryGridAdapter(Context context, List<StorageReference> images) {
        this.context = context;
        this.images = images;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object getItem(int position) {
        return images.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.grid_item, null);
        }

        ImageView imageView = convertView.findViewById(R.id.gridImage);
        images.get(position).getDownloadUrl().addOnCompleteListener(task -> {
            if (!isActivityDestroyed(imageView.getContext())) {
                Glide.with(imageView.getContext()).load(task.getResult()).centerCrop().into(imageView);
            }
        });

        return convertView;
    }

    private boolean isActivityDestroyed(Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return activity.isDestroyed() || activity.isFinishing();
        }
        return false;
    }
}
