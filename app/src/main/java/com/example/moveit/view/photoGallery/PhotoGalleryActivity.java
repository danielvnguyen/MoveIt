package com.example.moveit.view.photoGallery;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;
import com.example.moveit.R;
import com.example.moveit.databinding.ActivityPhotoGalleryBinding;
import com.example.moveit.model.GlobalUpdater;
import com.example.moveit.model.gallery.GalleryGridAdapter;
import com.example.moveit.model.gallery.ImageData;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("ALL")
public class PhotoGalleryActivity extends AppCompatActivity {

    private List<ImageData> images;
    private ActivityPhotoGalleryBinding binding;
    private FirebaseStorage storage;
    private FirebaseUser currentUser;
    private GalleryGridAdapter adapter;

    private FloatingActionButton addNewImgBtn;
    private FloatingActionButton takeImageBtn;
    private FloatingActionButton chooseFromGalleryBtn;

    private Animation rotateOpen;
    private Animation rotateClose;
    private Animation fromBottom;
    private Animation toBottom;
    private Boolean buttonClicked = false;

    private static final int IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private String currentPhotoPath;

    private Boolean isDefaultSort = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhotoGalleryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(getString(R.string.photo_gallery_title));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        storage = FirebaseStorage.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        setUpGallery();
        setUpAddImgButtons();
        setUpSortByBtn();
    }

    private void setUpAddImgButtons() {
        rotateOpen = AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim);
        rotateClose = AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim);
        fromBottom = AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim);
        toBottom = AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim);
        addNewImgBtn = findViewById(R.id.addNewImgBtn);
        takeImageBtn = findViewById(R.id.takeNewImageBtn);
        chooseFromGalleryBtn = findViewById(R.id.chooseFromGalleryBtn);

        addNewImgBtn.setOnClickListener(v -> {
            setVisibility(buttonClicked);
            setAnimation(buttonClicked);
            buttonClicked = !buttonClicked;
        });
        takeImageBtn.setOnClickListener(v -> {
            if (checkAndRequestPermissions()) {
                startCameraIntent();
            }
        });
        chooseFromGalleryBtn.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select image from here:"), IMAGE_REQUEST);
        });
    }

    private void startCameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File imageFile = null;
            try {
                imageFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (imageFile != null) {
                Uri imageUri = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider", imageFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private File createImageFile() throws IOException {
        String currentTimeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageName = "jpg_"+currentTimeStamp+"_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(imageName, ".jpg", storageDir);

        currentPhotoPath = imageFile.getAbsolutePath();
        return imageFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {
            Uri imageUri = data.getData();
            String imageId = UUID.randomUUID() + "." + getFileExtension(imageUri);
            final StorageReference fileRef = storage.getReference().child(currentUser.getUid())
                    .child("uploads").child(imageId);
            fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> setUpGallery());
        } else if (requestCode == CAMERA_REQUEST
                && resultCode == RESULT_OK) {
            File f = new File(currentPhotoPath);
            Uri imageUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", f);
            String imageId = UUID.randomUUID() + "." + getFileExtension(imageUri);
            final StorageReference fileRef = storage.getReference().child(currentUser.getUid())
                    .child("uploads").child(imageId);
            fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> setUpGallery());
        }
    }

    private boolean checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= 27) {
            int cameraPermission = ActivityCompat.checkSelfPermission(
                    PhotoGalleryActivity.this, Manifest.permission.CAMERA);
            if (cameraPermission == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(PhotoGalleryActivity.this,
                        new String[]{Manifest.permission.CAMERA}, 20);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 20 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCameraIntent();
        } else
            Toast.makeText(PhotoGalleryActivity.this, "Permission required to take photos",
                    Toast.LENGTH_SHORT).show();
    }

    private String getFileExtension(Uri imageUri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri));
    }

    private void setUpGallery() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        images = new ArrayList<>();
        StorageReference ref = storage.getReference().child(currentUser.getUid()).child("uploads");
        ref.listAll().addOnCompleteListener(task -> {
            for (StorageReference imageRef : Objects.requireNonNull(task.getResult()).getItems()) {
                imageRef.getMetadata().addOnSuccessListener(metadata -> {
                    ImageData imageData = new ImageData(imageRef, metadata.getCreationTimeMillis());
                    images.add(imageData);

                    if (images.size() == task.getResult().getItems().size()) {
                        if (isDefaultSort) {
                            images.sort((o1, o2) -> Long.compare(o2.getCreationTimeMillis(), o1.getCreationTimeMillis()));
                        } else {
                            images.sort((o1, o2) -> Long.compare(o1.getCreationTimeMillis(), o2.getCreationTimeMillis()));
                        }
                        List<StorageReference> sortedImages = new ArrayList<>();
                        for (ImageData img : images) {
                            sortedImages.add(img.getReference());
                            adapter = new GalleryGridAdapter(PhotoGalleryActivity.this, sortedImages);
                            binding.gridView.setAdapter(adapter);

                            binding.gridView.setOnItemClickListener((parent, view, position, id) -> {
                                Intent intent = ViewPhotoActivity.makeIntent(this);
                                Object currentItem = binding.gridView.getItemAtPosition(position);
                                String imageId = currentItem.toString().substring(currentItem.toString().indexOf("uploads/") + 8);
                                intent.putExtra("imageId", imageId);
                                intent.putExtra("showDelete", true);
                                startActivity(intent);
                            });
                        }
                    }
                    progressDialog.dismiss();
                });
            }
        });
    }

    private void setUpSortByBtn() {
        Button sortByBtn = findViewById(R.id.sortByBtn);
        sortByBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isDefaultSort = !isDefaultSort;
                if (isDefaultSort) {
                    sortByBtn.setText("Sort By: Newest");               
                } else {
                    sortByBtn.setText("Sort By: Oldest");
                }
                setUpGallery();
            }
        });
    }

    private void setVisibility(Boolean buttonClicked) {
        if (buttonClicked) {
            takeImageBtn.setVisibility(View.GONE);
            chooseFromGalleryBtn.setVisibility(View.GONE);
        } else {
            takeImageBtn.setVisibility(View.VISIBLE);
            chooseFromGalleryBtn.setVisibility(View.VISIBLE);
        }
    }

    private void setAnimation(Boolean buttonClicked) {
        if (buttonClicked) {
            takeImageBtn.startAnimation(toBottom);
            chooseFromGalleryBtn.startAnimation(toBottom);
            addNewImgBtn.startAnimation(rotateClose);
        } else {
            takeImageBtn.startAnimation(fromBottom);
            chooseFromGalleryBtn.startAnimation(fromBottom);
            addNewImgBtn.startAnimation(rotateOpen);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (GlobalUpdater.getInstance().isGalleryUpdated()) {
            setUpGallery();
            GlobalUpdater.getInstance().setGalleryUpdated(false);
        }
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