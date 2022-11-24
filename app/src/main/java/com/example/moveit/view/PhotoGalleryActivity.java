package com.example.moveit.view;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.example.moveit.R;
import com.example.moveit.databinding.ActivityPhotoGalleryBinding;
import com.example.moveit.model.GalleryGridAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class PhotoGalleryActivity extends AppCompatActivity {

    private ListResult images;
    private ActivityPhotoGalleryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhotoGalleryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(getString(R.string.photo_gallery_title));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        StorageReference ref = storage.getReference().child(currentUser.getUid()).child("uploads");
        ref.listAll().addOnCompleteListener(task -> {
            images = task.getResult();
            GalleryGridAdapter adapter = new GalleryGridAdapter(PhotoGalleryActivity.this, images);
            binding.gridView.setAdapter(adapter);

            binding.gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Toast.makeText(PhotoGalleryActivity.this, "Clicked", Toast.LENGTH_SHORT).show();
                }
            });
        });
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