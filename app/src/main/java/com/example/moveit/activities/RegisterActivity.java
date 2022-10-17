package com.example.moveit.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.moveit.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    private EditText emailInput;
    private EditText passwordInput;
    private Button registerBtn;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        registerBtn = findViewById(R.id.register);

        //Get Firebase Auth Instance:
        auth = FirebaseAuth.getInstance();

        setUpRegisterButton();
    }

    private void setUpRegisterButton() {
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailText = emailInput.getText().toString();
                String passwordText = passwordInput.getText().toString();

                if (TextUtils.isEmpty(emailText) || TextUtils.isEmpty(passwordText)) {
                    Toast.makeText(RegisterActivity.this, "Email or Password is empty", Toast.LENGTH_SHORT).show();
                } else if (passwordText.length() < 8) {
                    Toast.makeText(RegisterActivity.this, "Password is less than 8 characters", Toast.LENGTH_SHORT).show();
                } else {
                    registerUser(emailText, passwordText);
                }
            }
        });
    }

    private void registerUser(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Successfully registered", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, EntriesList.class));
                } else {
                    Toast.makeText(RegisterActivity.this, "Failed to register", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, RegisterActivity.class);
    }
}