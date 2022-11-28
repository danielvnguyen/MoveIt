package com.example.moveit.view;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.moveit.BuildConfig;
import com.example.moveit.R;

import java.util.Objects;

public class AboutInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_info);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.about);

        setUpButtons();
        setUpInterface();
    }

    @SuppressLint("SetTextI18n")
    private void setUpInterface() {
        TextView appVersion = findViewById(R.id.appVersionTV);
        float currentVer = BuildConfig.VERSION_CODE;

        appVersion.setText("MoveIt! Version: " + currentVer);
    }

    private void setUpButtons() {
        Button viewChangeLogsBtn = findViewById(R.id.changeLogBtn);
        Button contactSupportBtn = findViewById(R.id.contactSupportBtn);
        Button attributionsBtn = findViewById(R.id.attributionsBtn);
        ImageView gitHubBtn = findViewById(R.id.gitHubImageView);
        ImageView linkedInBtn = findViewById(R.id.linkedInImageView);

        linkedInBtn.setOnClickListener(v -> {
            Uri uri = Uri.parse(getString(R.string.linked_in_url));
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
        gitHubBtn.setOnClickListener(v -> {
            Uri uri = Uri.parse(getString(R.string.github_url));
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
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