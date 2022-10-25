package com.example.moveit.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.moveit.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

public class EntriesList extends AppCompatActivity {

    private Button logoutBtn;
    private FloatingActionButton addEntryBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entries_list);

        logoutBtn = findViewById(R.id.logout);
        addEntryBtn = findViewById(R.id.addEntry);

        setUpLogOutBtn();
        setUpAddEntryBtn();
    }

    private void setUpLogOutBtn() {
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(EntriesList.this, "Log Out Successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(EntriesList.this, StartActivity.class));
            }
        });
    }

    private void setUpAddEntryBtn() {
        addEntryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EntriesList.this, AddEntry.class));
            }
        });
    }
}