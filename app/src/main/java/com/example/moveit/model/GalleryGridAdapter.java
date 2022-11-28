package com.example.moveit.model;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.example.moveit.R;
import com.google.firebase.storage.ListResult;

public class GalleryGridAdapter extends BaseAdapter {
    private final Context context;
    private final ListResult images;
    private LayoutInflater inflater;

    public GalleryGridAdapter(Context context, ListResult images) {
        this.context = context;
        this.images = images;
    }

    @Override
    public int getCount() {
        return images.getItems().size();
    }

    @Override
    public Object getItem(int position) {
        return images.getItems().get(position);
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
        images.getItems().get(position).getDownloadUrl().addOnCompleteListener(task ->
                Glide.with(imageView.getContext()).load(task.getResult()).centerCrop().into(imageView));

        return convertView;
    }
}
