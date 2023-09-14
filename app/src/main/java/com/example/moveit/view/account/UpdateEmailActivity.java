package com.example.moveit.view.account;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.moveit.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Objects;

public class UpdateEmailActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private EditText emailInput;
    private EditText currentPasswordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.update_email_title));

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        emailInput = findViewById(R.id.newEmailInput);
        currentPasswordInput = findViewById(R.id.currentPassword);

        setUpShowHideBtn();
        setUpUpdateBtn();
    }

    private void setUpShowHideBtn() {
        Button currentShowHideBtn = findViewById(R.id.currentPasswordShowHideBtn);
        currentShowHideBtn.setOnClickListener(v -> {
            if(currentShowHideBtn.getText().toString().equals("Show")){
                currentPasswordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                currentShowHideBtn.setText(R.string.hide);
            } else{
                currentPasswordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                currentShowHideBtn.setText(R.string.show);
            }
        });
    }

    private void setUpUpdateBtn() {
        Button updateBtn = findViewById(R.id.updateButton);
        updateBtn.setOnClickListener(view -> {
            String currentPasswordText = currentPasswordInput.getText().toString();
            String newEmailText = emailInput.getText().toString();
            if (TextUtils.isEmpty(newEmailText) || TextUtils.isEmpty(currentPasswordText)) {
                Toast.makeText(UpdateEmailActivity.this, "Please fill in both inputs", Toast.LENGTH_SHORT).show();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(newEmailText).matches()) {
                Toast.makeText(UpdateEmailActivity.this, "Email has invalid format!", Toast.LENGTH_SHORT).show();
            } else {
                auth.signInWithEmailAndPassword(Objects.requireNonNull(currentUser.getEmail()), currentPasswordText)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            currentUser.updateEmail(newEmailText).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(UpdateEmailActivity.this, "Email updated successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(UpdateEmailActivity.this, "Failed to update email", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(UpdateEmailActivity.this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        });
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, UpdateEmailActivity.class);
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