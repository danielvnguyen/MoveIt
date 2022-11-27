package com.example.moveit.view.photoGallery;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.moveit.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
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
        ImageView deleteImgBtn = findViewById(R.id.deleteImgBtn);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        deleteImgBtn.setOnClickListener(v -> {
            db.collection("meals").document(currentUser.getUid())
                    .collection("mealList").get().addOnSuccessListener(queryDocumentSnapshots -> {
                        for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                            String imageId = Objects.requireNonNull(queryDocumentSnapshots.getDocuments().get(i).get("imageId")).toString();
                            if (imageId.equals(currentImageId)) {
                                String mealId = queryDocumentSnapshots.getDocuments().get(i).getId();
                                db.collection("meals").document(currentUser.getUid())
                                        .collection("mealList").document(mealId).update("imageId", "");
                            }
                        }
                    });

            final StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(currentUser.getUid())
                    .child("uploads").child(currentImageId);
            imageRef.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ViewPhotoActivity.this, "Successfully deleted image", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ViewPhotoActivity.this, "Failed to deleted image", Toast.LENGTH_SHORT).show();
                }
            });
        });

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
                Picasso.with(currentImageView.getContext()).load(url).noFade().fit().centerInside().into(currentImageView);
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