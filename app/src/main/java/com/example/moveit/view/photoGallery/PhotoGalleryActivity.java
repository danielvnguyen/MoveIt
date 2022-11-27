package com.example.moveit.view.photoGallery;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.example.moveit.R;
import com.example.moveit.databinding.ActivityPhotoGalleryBinding;
import com.example.moveit.model.GalleryGridAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class PhotoGalleryActivity extends AppCompatActivity {

    private ListResult images;
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

        });
        chooseFromGalleryBtn.setOnClickListener(v -> {

        });
    }

    private void setUpGallery() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading...");
        progressDialog.show();

        StorageReference ref = storage.getReference().child(currentUser.getUid()).child("uploads");
        ref.listAll().addOnCompleteListener(task -> {
            images = task.getResult();
            adapter = new GalleryGridAdapter(PhotoGalleryActivity.this, images);
            binding.gridView.setAdapter(adapter);
            progressDialog.dismiss();

            binding.gridView.setOnItemClickListener((parent, view, position, id) -> {
                Intent intent = ViewPhotoActivity.makeIntent(this);
                Object currentItem = binding.gridView.getItemAtPosition(position);
                String imageId = currentItem.toString().substring(currentItem.toString().indexOf("uploads/") + 8);
                intent.putExtra("imageId", imageId);
                startActivity(intent);
            });
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
        setUpGallery();
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