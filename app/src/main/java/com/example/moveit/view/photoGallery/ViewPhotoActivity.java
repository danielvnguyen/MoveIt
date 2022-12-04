package com.example.moveit.view.photoGallery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.moveit.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class ViewPhotoActivity extends AppCompatActivity {
    private FirebaseUser currentUser;
    private String currentImageId;
    private ImageView currentImageView;
    private static final Integer REQUEST_CODE = 1;

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

        deleteImgBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle(R.string.confirm_delete_photo);
            builder.setMessage(R.string.no_takesies_backsies);
            builder.setPositiveButton(R.string.yes, (dialog, which) -> handleDelete());
            builder.setNegativeButton(R.string.no, (dialog, which) -> {});
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        downloadImgBtn.setOnClickListener(v -> {
            final StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(currentUser.getUid())
                    .child("uploads").child(currentImageId);
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                if (ContextCompat.checkSelfPermission(ViewPhotoActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    saveImage();
                } else {
                    ActivityCompat.requestPermissions(ViewPhotoActivity.this, new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
                }
            });
        });
    }

    private void handleDelete() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
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
    }

    private void saveImage() {
        Uri images;
        ContentResolver contentResolver = getContentResolver();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            images = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        String imageTitle = System.currentTimeMillis() + ".jpg";

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, imageTitle);
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "images/*");
        Uri uri = contentResolver.insert(images, contentValues);
        try {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) currentImageView.getDrawable();
            Bitmap bitmap = bitmapDrawable.getBitmap();

            OutputStream outputStream = contentResolver.openOutputStream(uri);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            Toast.makeText(ViewPhotoActivity.this, "Imaged saved to phone successfully",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(ViewPhotoActivity.this, "Failed to save image to phone",
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveImage();
            } else {
                Toast.makeText(ViewPhotoActivity.this, "Please provide required permission to download images",
                        Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void setUpInterface() {
        currentImageView = findViewById(R.id.currentImage);
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
                    String dateTimePattern = "MMM dd, yyyy h:mm a";
                    @SuppressLint("SimpleDateFormat") String dateText = new SimpleDateFormat(dateTimePattern).format(date);
                    dateTimeTV.setText(dateText);
                    dateTimeTV.setVisibility(View.VISIBLE);
                }
            });
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(this).load(uri).centerInside().into(currentImageView);
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