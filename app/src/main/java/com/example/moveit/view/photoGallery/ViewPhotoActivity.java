package com.example.moveit.view.photoGallery;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.moveit.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class ViewPhotoActivity extends AppCompatActivity {
    private FirebaseUser currentUser;
    private String currentImageId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo);
        setTitle(getString(R.string.view_photo_title));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        setUpInterface();
        setUpButtons();
    }

    private void setUpButtons() {
        ImageView downloadImgBtn = findViewById(R.id.downloadImgBtn);

        downloadImgBtn.setOnClickListener(v -> {
            final StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(currentUser.getUid())
                    .child("uploads").child(currentImageId);
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {

            });
        });
    }

    private void setUpInterface() {
        ImageView currentImageView = findViewById(R.id.currentImage);
        TextView dateTimeTV = findViewById(R.id.dateTimeTV);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            currentImageId = extras.getString("imageId");
            final StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(currentUser.getUid())
                    .child("uploads").child(currentImageId);
            fileRef.getMetadata().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    StorageMetadata metadata = task.getResult();
                    assert metadata != null;
                    long dateTime = metadata.getCreationTimeMillis();
                    Date date = new Date(dateTime);
                    String dateTimePattern = "MMM dd, yyyy EEE h:mm a";
                    @SuppressLint("SimpleDateFormat") String dateText = new SimpleDateFormat(dateTimePattern).format(date);
                    dateTimeTV.setText(dateText);
                    dateTimeTV.setVisibility(View.VISIBLE);
                }
            });
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String url = uri.toString();
                Picasso.with(currentImageView.getContext()).load(url).noFade().fit().into(currentImageView);
                currentImageView.setVisibility(View.VISIBLE);
            });
        }
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, ViewPhotoActivity.class);
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
}