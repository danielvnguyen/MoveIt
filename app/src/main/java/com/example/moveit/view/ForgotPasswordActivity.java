package com.example.moveit.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.moveit.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class ForgotPasswordActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText emailInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.forgot_password_title));
        auth = FirebaseAuth.getInstance();

        emailInput = findViewById(R.id.email);

        setUpSendRequestBtn();
    }

    private void setUpSendRequestBtn() {
        Button sendRequestBtn = findViewById(R.id.sendRequestBtn);
        sendRequestBtn.setOnClickListener(v -> {
            String emailText = String.valueOf(emailInput.getText());
            if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                Toast.makeText(ForgotPasswordActivity.this, "Email has invalid format!", Toast.LENGTH_SHORT).show();
            } else {
                auth.sendPasswordResetEmail(emailText).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this, "Password reset request sent successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, "Failed to send password reset request", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, ForgotPasswordActivity.class);
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